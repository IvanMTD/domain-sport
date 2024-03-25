package ru.fcpsr.domainsport.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.services.EkpService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/event")
public class Event {

    private final EkpService ekpService;

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
}
