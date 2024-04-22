package ru.fcpsr.domainsport.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.NewsDTO;
import ru.fcpsr.domainsport.models.AppUser;
import ru.fcpsr.domainsport.services.NewsService;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/list")
    public Mono<Rendering> getNewsList(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Новости спорта")
                        .modelAttribute("description", "Новости спортивной индустрии")
                        .modelAttribute("index","news-list-page")
                        .modelAttribute("newsList",newsService.getAllSortedById().take(4))
                        .build()
        );
    }

    @GetMapping("/add")
    @PreAuthorize("@AccessService.isAdmin(#authentication)")
    public Mono<Rendering> addNewsPage(@AuthenticationPrincipal Authentication authentication){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Добавить новость")
                        .modelAttribute("index", "news-add-page")
                        .modelAttribute("newsForm", new NewsDTO())
                        .build()
        );
    }

    @PostMapping("/save")
    @PreAuthorize("@AccessService.isAdmin(#authentication)")
    public Mono<Rendering> saveNews(@AuthenticationPrincipal Authentication authentication, @ModelAttribute(name = "newsForm") @Valid NewsDTO newsDTO, Errors errors){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Добавить новость")
                            .modelAttribute("index", "news-add-page")
                            .modelAttribute("newsForm", newsDTO)
                            .build()
            );
        }

        AppUser user = (AppUser) authentication.getPrincipal();
        return newsService.createNews(newsDTO, user).flatMap(news -> {
            return Mono.just(Rendering.redirectTo("/").build());
        });
    }

    @GetMapping("/show")
    public Mono<Rendering> newsPage(@RequestParam(name = "news") long newsId){
        return newsService.getById(newsId).flatMap(news -> {
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title", news.getTitle())
                            .modelAttribute("description", news.getAnnotation())
                            .modelAttribute("index","news-page")
                            .modelAttribute("news", news)
                            .build()
            );
        }).defaultIfEmpty(Rendering.redirectTo("/error").build());
    }

    @GetMapping("/delete")
    @PreAuthorize("@AccessService.isAdmin(#authentication)")
    public Mono<Rendering> deleteNews(@AuthenticationPrincipal Authentication authentication, @RequestParam(name = "news") long newsId){
        return newsService.deleteById(newsId).flatMap(news -> Mono.just(Rendering.redirectTo("/").build()));
    }

    @GetMapping("/edit")
    @PreAuthorize("@AccessService.isAdmin(#authentication)")
    public Mono<Rendering> editNewsPage(@AuthenticationPrincipal Authentication authentication, @RequestParam(name = "news") long newsId){
        return newsService.getById(newsId).flatMap(news -> {
            NewsDTO newsDTO = new NewsDTO(news);
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Страница редактирования")
                            .modelAttribute("index", "news-edit-page")
                            .modelAttribute("newsForm", newsDTO)
                            .build()
            );
        });
    }

    @PostMapping("/update")
    @PreAuthorize("@AccessService.isAdmin(#authentication)")
    public Mono<Rendering> updateNews(@AuthenticationPrincipal Authentication authentication, @ModelAttribute(name = "newsForm") @Valid NewsDTO newsDTO, Errors errors){
        return newsService.getById(newsDTO.getId()).flatMap(news -> {
            if(errors.hasErrors()){
                newsDTO.setPlacedAt(news.getPlacedAt());
                newsDTO.setId(news.getId());
                return Mono.just(
                        Rendering.view("template")
                                .modelAttribute("title","Страница редактирования")
                                .modelAttribute("index", "news-edit-page")
                                .modelAttribute("newsForm", newsDTO)
                                .build()
                );
            }

            return newsService.updatedNews(newsDTO).flatMap(newsUpdated -> {
                log.info("NEWS TOTALLY SAVED {}", newsUpdated.getId());
                return Mono.just(Rendering.redirectTo("/news/show?news=" + newsUpdated.getId()).build());
            });
        });
    }
}
