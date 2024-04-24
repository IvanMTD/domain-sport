package ru.fcpsr.domainsport.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.SchoolDTO;
import ru.fcpsr.domainsport.services.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/school")
public class SchoolController {
    private final SchoolService schoolService;
    private final AccessService accessService;
    private final GeocodeService geocodeService;
    private final MinioService minioService;
    private final MinioFileService fileService;

    @GetMapping("/list")
    public Mono<Rendering> schoolListPage(@AuthenticationPrincipal Authentication authentication, @RequestParam(name = "page") int page, @RequestParam(name = "size") int size, @RequestParam(name = "search") String search){
        return schoolService.getCountBySubject(search).flatMap(count -> {
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
                            .modelAttribute("title","School list")
                            .modelAttribute("index", "school-list-page")
                            .modelAttribute("schools",schoolService.getAllBySearch(PageRequest.of(pageControl,size),search))
                            .modelAttribute("search",search)
                            .modelAttribute("lastPage",lastPage)
                            .modelAttribute("page",pageControl)
                            .modelAttribute("size", size)
                            .modelAttribute("accessCreate", accessService.getAccess(authentication, "SCHOOL", "CREATE"))
                            .build()
            );
        });
    }

    @GetMapping("/show")
    public Mono<Rendering> showSchoolPage(@AuthenticationPrincipal Authentication authentication, @RequestParam(name = "school") long schoolId){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","School page")
                        .modelAttribute("index","school-page")
                        .modelAttribute("school", schoolService.getById(schoolId))
                        .modelAttribute("accessUpdate", accessService.getAccess(authentication,"SCHOOL","UPDATE"))
                        .modelAttribute("accessDelete", accessService.getAccess(authentication,"SCHOOL","DELETE"))
                        .build()
        );
    }

    @GetMapping("/add")
    @PreAuthorize("@AccessService.getAccess(#authentication,'SCHOOL','CREATE')")
    public Mono<Rendering> addSchoolPage(@AuthenticationPrincipal Authentication authentication){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Добавить спортивную школу")
                        .modelAttribute("index","school-add-page")
                        .modelAttribute("school", new SchoolDTO())
                        .build()
        );
    }

    @PostMapping("/add")
    @PreAuthorize("@AccessService.getAccess(#authentication,'SCHOOL','CREATE')")
    public Mono<Rendering> addSchool(@AuthenticationPrincipal Authentication authentication, @ModelAttribute(name = "school") @Valid SchoolDTO schoolDTO, Errors errors){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Добавить спортивную школу")
                            .modelAttribute("index","school-add-page")
                            .modelAttribute("school", schoolDTO)
                            .build()
            );
        }else{
            return schoolService.createSchool(schoolDTO).flatMap(firstStepSchool -> {
                log.info("school has been pre saved: {}", firstStepSchool);
                return geocodeService.getResponse(firstStepSchool.getAddress()).flatMap(response -> {
                    String pos = response.getResponse().getGeoObjectCollection().getFeatureMember().get(0).getGeoObject().getPoint().getPos();
                    String[] part = pos.split(" ");
                    float s = Float.parseFloat(part[1]);
                    float d = Float.parseFloat(part[0]);
                    firstStepSchool.setS(s);
                    firstStepSchool.setD(d);
                    return Mono.just(firstStepSchool);
                }).switchIfEmpty(Mono.just(firstStepSchool));
            }).flatMap(secondStepSchool -> {
                log.info("second step add image");
                if(schoolDTO.getImage() != null){
                    return minioService.uploadImage(schoolDTO.getImage()).flatMap(minioResponse -> {
                        log.info("image saved in minio with data {}", minioResponse.getResponse().etag());
                        return fileService.save(minioResponse, secondStepSchool.getId()).flatMap(minioFile -> {
                            log.info("information saved in database with data {}", minioFile.getId());
                            secondStepSchool.setPhotoId(minioFile.getId());
                            return Mono.just(secondStepSchool);
                        });
                    });
                }else{
                    log.info("image not found");
                    return Mono.just(secondStepSchool);
                }
            }).flatMap(lastStepSchool -> {
                log.info("last step try add logo");
                if(schoolDTO.getLogo() != null){
                    return minioService.uploadImage(schoolDTO.getLogo()).flatMap(minioResponse -> {
                        log.info("logo saved in minio with data {}", minioResponse.getResponse().etag());
                        return fileService.save(minioResponse, lastStepSchool.getId()).flatMap(minioFile -> {
                            log.info("information saved in database with data {}", minioFile.getId());
                            lastStepSchool.setLogoId(minioFile.getId());
                            return schoolService.save(lastStepSchool);
                        });
                    });
                }else{
                    log.info("logo not found");
                    return schoolService.save(lastStepSchool);
                }
            }).flatMap(school -> {
                log.info("school totally saved with data {}", school);
                return Mono.just(
                        Rendering.redirectTo("/school/list?page=0&size=12&search=all").build()
                );
            });
        }
    }

    @GetMapping("/edit")
    @PreAuthorize("@AccessService.getAccess(#authentication,'SCHOOL','UPDATE')")
    public Mono<Rendering> schoolEditPage(@AuthenticationPrincipal Authentication authentication, @RequestParam(name = "school") long id){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Страница редактирования школы")
                        .modelAttribute("index","school-edit-page")
                        .modelAttribute("school", schoolService.getById(id).flatMap(school -> Mono.just(new SchoolDTO(school))))
                        .build()
        );
    }

    @PostMapping("/edit")
    @PreAuthorize("@AccessService.getAccess(#authentication,'SCHOOL','UPDATE')")
    public Mono<Rendering> schoolEdit(@AuthenticationPrincipal Authentication authentication, @ModelAttribute(name = "school") @Valid SchoolDTO schoolDTO, Errors errors){
        return Mono.just(Rendering.redirectTo("/").build());
    }
}
