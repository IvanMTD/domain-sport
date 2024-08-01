package ru.fcpsr.domainsport.controllers.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.DisciplineDTO;
import ru.fcpsr.domainsport.dto.EkpDTO;
import ru.fcpsr.domainsport.dto.SportDTO;
import ru.fcpsr.domainsport.dto.SportObjectDTO;
import ru.fcpsr.domainsport.models.Discipline;
import ru.fcpsr.domainsport.models.News;
import ru.fcpsr.domainsport.services.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@org.springframework.web.bind.annotation.RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RestController {
    private final EkpService ekpService;
    private final SportService sportService;
    private final DisciplineService disciplineService;
    private final MinioFileService fileService;
    private final MinioService minioService;
    private final EventService eventService;
    private final SportObjectService sportObjectService;
    private final CacheService cacheService;

    private final NewsService newsService;
    private final SchoolService schoolService;

    @GetMapping("/get/cache")
    public Flux<Object> getCache(@RequestParam(name = "cacheName") String cacheName){
        return cacheService.checkCacheContents(cacheName).flatMap(cache -> {
            return Mono.just(cache);
        });
    }
    @GetMapping("/get/ekp/by/date")
    public Flux<EkpDTO> findByDate(@RequestParam(name = "query") String query){
        return ekpService.getByDate(query);
    }

    @GetMapping("/get/ekp/all")
    public Flux<EkpDTO> ekpGetAll(){
        return ekpService.getAll().flatMap(ekp -> Mono.just(new EkpDTO(ekp)));
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

    @GetMapping("/get/schools")
    public Flux<String> findBySubject(@RequestParam(name = "query") String query){
        return schoolService.getAllBySubject(query).take(10);
    }

    @GetMapping("/get/next/object")
    public Flux<SportObjectDTO> getNext(@RequestParam(name = "stack") String stack){
        int page = Integer.parseInt(stack);
        return sportObjectService.getAllSortedById(PageRequest.of(page,12)).flatMapSequential(sportObject -> Mono.just(new SportObjectDTO(sportObject)));
    }

    @GetMapping("/get/next/news")
    public Flux<News> getNews(@RequestParam(name = "stack") String stack){
        return newsService.getAllSortedById(PageRequest.of(Integer.parseInt(stack),4));
    }

    @GetMapping("/search/news")
    public Flux<News> searchNews(@RequestParam(name = "search") String search){
        return newsService.searchNews(search).take(12);
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
