package ru.fcpsr.domainsport.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.EkpDTO;
import ru.fcpsr.domainsport.enums.Status;
import ru.fcpsr.domainsport.services.EkpService;
import ru.fcpsr.domainsport.services.GeocodeService;
import ru.fcpsr.domainsport.services.MinioFileService;
import ru.fcpsr.domainsport.services.MinioService;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/event")
public class Event {

    private final EkpService ekpService;
    private final GeocodeService geocodeService;
    private final MinioService minioService;
    private final MinioFileService fileService;

    @GetMapping("/all")
    public Mono<Rendering> eventsPage(@RequestParam(name = "page") int page, @RequestParam(name = "size") int size){
        return ekpService.getCount().flatMap(count -> {
            int pageControl = page;
            if(pageControl < 0){
                pageControl = 0;
            }
            long lastPage = count / size;
            if(pageControl >= lastPage){
                pageControl = (int)lastPage;
            }
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title", "Events page")
                            .modelAttribute("index", "events-page")
                            .modelAttribute("events", ekpService.getAll(PageRequest.of(pageControl,size)))
                            .modelAttribute("lastPage", lastPage)
                            .modelAttribute("page", pageControl)
                            .modelAttribute("size", size)
                            .build()
            );
        });
    }

    @GetMapping("/add")
    public Mono<Rendering> eventAddPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Event add")
                        .modelAttribute("index","event-add-page")
                        .modelAttribute("event",new EkpDTO())
                        .modelAttribute("statusList", Status.values())
                        .build()
        );
    }

    @PostMapping("/add")
    public Mono<Rendering> eventAdd(@ModelAttribute(name = "event") @Valid EkpDTO ekpDTO, Errors errors){
        if(ekpDTO.getEkp() == null && ekpDTO.getNum() == null){
            errors.rejectValue("ekp","","Укажите оба или один из вариантов ЕКП или Иной номер");
        }
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Event add")
                            .modelAttribute("index","event-add-page")
                            .modelAttribute("event", ekpDTO)
                            .modelAttribute("statusList", Status.values())
                            .build()
            );
        }

        return geocodeService.getResponse(ekpDTO).flatMap(response -> {
            String pos = response.getResponse().getGeoObjectCollection().getFeatureMember().get(0).getGeoObject().getPoint().getPos();
            String[] part = pos.split(" ");
            float s = Float.parseFloat(part[1]);
            float d = Float.parseFloat(part[0]);
            ekpDTO.setS(s);
            ekpDTO.setD(d);
            return ekpService.save(ekpDTO).flatMap(ekp -> {
                log.info(ekp.toString());
                return minioService.uploadImage(ekpDTO.getLogo()).flatMap(minioResponse -> {
                    log.info(minioResponse.toString());
                    return fileService.save(minioResponse, ekp.getId()).flatMap(minioFile -> {
                        if(minioFile.getMid() != 0){
                            ekp.setLogo(minioFile.getId());
                            return ekpService.save(ekp);
                        }else{
                            return Mono.just(ekp);
                        }
                    });
                });
            }).flatMap(ekp -> {
                log.info(ekp.toString());
                return minioService.uploadImage(ekpDTO.getImage()).flatMap(minioResponse -> {
                    log.info(minioResponse.toString());
                    return fileService.save(minioResponse, ekp.getId()).flatMap(minioFile -> {
                        if(minioFile.getMid() != 0){
                            ekp.setImage(minioFile.getId());
                            return ekpService.save(ekp);
                        }else{
                            return Mono.just(ekp);
                        }
                    });
                });
            }).flatMap(ekp -> {
                log.info("ekp saved " + ekp.toString());
                return Mono.just(Rendering.redirectTo("/").build());
            });
        });
    }
}
