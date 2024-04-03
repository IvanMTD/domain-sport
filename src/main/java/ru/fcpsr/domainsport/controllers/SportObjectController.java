package ru.fcpsr.domainsport.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@RequestMapping("/object")
public class SportObjectController {
    @GetMapping("/all")
    public Mono<Rendering> sportObjectsPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Поиск спортивных организаций по всей России для проведения соревнований")
                        .modelAttribute("description","Спортивные организации России")
                        .modelAttribute("index","object-all-page")
                        .build()
        );
    }
}
