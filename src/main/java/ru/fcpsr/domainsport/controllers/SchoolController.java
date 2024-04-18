package ru.fcpsr.domainsport.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.services.SchoolService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/school")
public class SchoolController {
    private final SchoolService schoolService;

    @GetMapping("/list")
    public Mono<Rendering> schoolListPage(@RequestParam(name = "page") int page, @RequestParam(name = "size") int size, @RequestParam(name = "search") String search){
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
                            .build()
            );
        });
    }

    @GetMapping("/show")
    public Mono<Rendering> showSchoolPage(@RequestParam(name = "school") long schoolId){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","School page")
                        .modelAttribute("index","school-page")
                        .modelAttribute("school", schoolService.getById(schoolId))
                        .build()
        );
    }
}
