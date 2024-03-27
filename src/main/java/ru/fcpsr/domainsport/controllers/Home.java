package ru.fcpsr.domainsport.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.services.EkpService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class Home {

    private final EkpService ekpService;

    @GetMapping("/")
    public Mono<Rendering> homePage(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Home page")
                        .modelAttribute("index","home-page")
                        .modelAttribute("ekpList", ekpService.getByDate(LocalDate.now()))
                        .modelAttribute("eventList", ekpService.getAll().take(4))
                        .modelAttribute("currentDate", LocalDate.now().format(formatter))
                        .build()
        );
    }
}
