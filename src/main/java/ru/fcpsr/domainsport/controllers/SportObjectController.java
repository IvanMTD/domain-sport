package ru.fcpsr.domainsport.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.SportObjectDTO;
import ru.fcpsr.domainsport.services.SportObjectService;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/object")
public class SportObjectController {
    private final SportObjectService sportObjectService;
    @GetMapping("/all")
    public Mono<Rendering> sportObjectsPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Поиск спортивных организаций по всей России для проведения соревнований")
                        .modelAttribute("description","Спортивные организации России")
                        .modelAttribute("index","object-all-page")
                        .modelAttribute("objects",sportObjectService.getAllSortedById(PageRequest.of(0,12)).flatMapSequential(sportObject -> Mono.just(new SportObjectDTO(sportObject))))
                        .build()
        );
    }

    @GetMapping("/show")
    public Mono<Rendering> showObjectPage(@RequestParam(name = "object") long object){
        return sportObjectService.getObjectById(object).flatMap(sportObject -> {
            log.info("found object {} in db", sportObject);
            SportObjectDTO sportObjectDTO = new SportObjectDTO(sportObject);
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title",sportObject.getTitle())
                            .modelAttribute("description", sportObject.getTitle() + " | " + sportObject.getAddress())
                            .modelAttribute("index", "object-show-page")
                            .modelAttribute("object",sportObjectDTO)
                            .build()
            );
        });
    }
}
