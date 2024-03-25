package ru.fcpsr.domainsport.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.models.AppUser;
import ru.fcpsr.domainsport.services.UserService;

@ControllerAdvice
@RequiredArgsConstructor
public class SecurityAdvice {

    private final UserService userService;

    @ModelAttribute("auth")
    public Mono<Boolean> isAuthenticate(@AuthenticationPrincipal Authentication authentication){
        return Mono.just(authentication != null);
    }

    @ModelAttribute("principal")
    public Mono<AppUser> principal(@AuthenticationPrincipal Authentication authentication){
        if(authentication != null) {
            String pattern = authentication.getPrincipal().toString().substring(0,7);
            if(pattern.equals("AppUser")){
                AppUser appUser = (AppUser) authentication.getPrincipal();
                return userService.getUserByUsername(appUser.getUsername());
            }else{
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                return userService.getUserByUsername(oAuth2User.getAttribute("psuid"));
            }
        }
        return Mono.empty();
    }
}
