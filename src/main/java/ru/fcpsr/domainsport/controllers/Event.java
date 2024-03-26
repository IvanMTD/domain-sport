package ru.fcpsr.domainsport.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.EkpDTO;
import ru.fcpsr.domainsport.enums.Status;
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
    public Mono<Rendering> eventAdd(@ModelAttribute(name = "event") EkpDTO ekpDTO){
        System.out.println(ekpDTO);
        return Mono.just(Rendering.redirectTo("/event/add").build());
    }
}
