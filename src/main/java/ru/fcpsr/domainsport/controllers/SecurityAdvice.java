package ru.fcpsr.domainsport.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.enums.Role;
import ru.fcpsr.domainsport.models.AppUser;
import ru.fcpsr.domainsport.services.UserService;

import java.util.Collection;

@ControllerAdvice
@RequiredArgsConstructor
public class SecurityAdvice {

    private final UserService userService;

    @ModelAttribute("auth")
    public Mono<Boolean> isAuthenticate(@AuthenticationPrincipal Authentication authentication){
        return Mono.just(authentication != null);
    }

    @ModelAttribute("admin")
    public Mono<Boolean> isAdmin(@AuthenticationPrincipal Authentication authentication){
        if(authentication != null) {
            String pattern = authentication.getPrincipal().toString().substring(0,7);
            if(pattern.equals("AppUser")){
                AppUser appUser = (AppUser) authentication.getPrincipal();
                return Mono.just(appUser.getRole().equals(Role.ADMIN));
            }else{
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                return userService.getUserByOauthId(oAuth2User.getAttribute("psuid")).flatMap(appUser -> Mono.just(appUser.getRole().equals(Role.ADMIN)));
            }
        }
        return Mono.empty();
    }

    @ModelAttribute("manager")
    public Mono<Boolean> isManager(@AuthenticationPrincipal Authentication authentication){
        if(authentication != null) {
            String pattern = authentication.getPrincipal().toString().substring(0,7);
            if(pattern.equals("AppUser")){
                AppUser appUser = (AppUser) authentication.getPrincipal();
                return Mono.just(appUser.getRole().equals(Role.MANAGER));
            }else{
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                return userService.getUserByOauthId(oAuth2User.getAttribute("psuid")).flatMap(appUser -> Mono.just(appUser.getRole().equals(Role.MANAGER)));
            }
        }
        return Mono.empty();
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
                return userService.getUserByOauthId(oAuth2User.getAttribute("psuid")).flatMap(user -> ReactiveSecurityContextHolder.getContext().flatMap(securityContext -> {
                    Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    securityContext.setAuthentication(auth);
                    return Mono.just(user);
                }));
            }
        }
        return Mono.empty();
    }
}
