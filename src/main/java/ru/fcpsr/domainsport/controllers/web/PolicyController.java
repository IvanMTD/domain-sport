package ru.fcpsr.domainsport.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@RequestMapping("/policy")
public class PolicyController {
    @GetMapping("/secure")
    public Mono<Rendering> securityPolicyPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Политика безопасности")
                        .modelAttribute("index","policy-secure-page")
                        .build()
        );
    }
}
