package ru.fcpsr.domainsport.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.DisciplineDTO;
import ru.fcpsr.domainsport.dto.EkpDTO;
import ru.fcpsr.domainsport.dto.SportDTO;
import ru.fcpsr.domainsport.models.Discipline;
import ru.fcpsr.domainsport.services.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class Rest {
    private final EkpService ekpService;
    private final SportService sportService;
    private final DisciplineService disciplineService;
    private final MinioFileService fileService;
    private final MinioService minioService;
    private final EventService eventService;
    @GetMapping("/get/ekp/by/date")
    public Flux<EkpDTO> findByDate(@RequestParam(name = "query") String query){
        return ekpService.getByDate(query);
    }

    @GetMapping("/get/ekp/by/sport/date")
    public Flux<EkpDTO> findBySportDate(@RequestParam(name = "sport") String sportTitle, @RequestParam(name = "date") String date){
        return sportService.getByTitle(sportTitle).flatMapMany(sport -> {
            log.info("sport {} found", sport.getTitle());
            return ekpService.getAllBySportIdAndDate(sport.getId(),date).flatMap(eventService::completedEvent);
        });
    }

    @GetMapping("/get/ekp/by/sport/period")
    public Flux<EkpDTO> getBySportPeriod(@RequestParam(name = "sport") String sportTitle, @RequestParam(name = "begin") String begin, @RequestParam(name = "end") String end){
        return sportService.getByTitle(sportTitle).flatMapMany(sport -> {
            log.info("sport {} found", sport.getTitle());
            return ekpService.getAllBySportIdAndRangeDates(sport.getId(),begin,end).flatMap(eventService::completedEvent);
        });
    }

    @GetMapping("/get/ekp/by/sport")
    public Flux<EkpDTO> findBySport(@RequestParam(name = "query") String query){
        return sportService.getByTitle(query).flatMapMany(sport -> {
            if(sport.getId() == 0){
                return Flux.empty();
            }else{
                return ekpService.getAllBySportId(sport.getId()).flatMap(ekp -> {
                    EkpDTO ekpDTO = new EkpDTO(ekp);
                    return disciplineService.getAllByIds(sport.getDisciplineIds()).flatMap(discipline -> {
                        DisciplineDTO disciplineDTO = new DisciplineDTO(discipline);
                        return Mono.just(disciplineDTO);
                    }).collectList().flatMapMany(dl -> {
                        dl = dl.stream().sorted(Comparator.comparing(DisciplineDTO::getTitle)).collect(Collectors.toList());
                        SportDTO sportDTO = new SportDTO(sport);
                        sportDTO.setDisciplines(dl);
                        ekpDTO.setSport(sportDTO);
                        return Mono.just(ekpDTO);
                    });
                });
            }
        });
    }

    @GetMapping("/get/sports")
    public Flux<SportDTO> findByTitle(@RequestParam(name = "query") String query){
        return sportService.getAllByTitleLike(query).flatMap(sport -> {
            SportDTO sportDTO = new SportDTO(sport);
            return disciplineService.getAllByIds(sport.getDisciplineIds()).collectList().flatMap(disciplines -> {
                List<DisciplineDTO> disciplineList = new ArrayList<>();
                for(Discipline discipline : disciplines){
                    disciplineList.add(new DisciplineDTO(discipline));
                }
                sportDTO.setDisciplines(disciplineList);
                return Mono.just(sportDTO);
            });
        }).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(SportDTO::getTitle)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just).take(10);
    }

    @ResponseBody
    @GetMapping("/download")
    public Mono<ResponseEntity<Mono<InputStreamResource>>> download(@RequestParam(name = "image") long id){
        return fileService.findById(id).map(fileInfo -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileInfo.getUid())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(minioService.download(fileInfo)));
    }
}
