package ru.fcpsr.domainsport.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.DisciplineDTO;
import ru.fcpsr.domainsport.dto.EkpDTO;
import ru.fcpsr.domainsport.dto.SportDTO;
import ru.fcpsr.domainsport.models.Discipline;
import ru.fcpsr.domainsport.services.DisciplineService;
import ru.fcpsr.domainsport.services.EkpService;
import ru.fcpsr.domainsport.services.SportService;

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
    @GetMapping("/get/date")
    public Flux<EkpDTO> findByDate(@RequestParam(name = "query") String query){
        return ekpService.getByDate(query);
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
}
