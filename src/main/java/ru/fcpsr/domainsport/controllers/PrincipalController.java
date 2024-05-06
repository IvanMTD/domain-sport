package ru.fcpsr.domainsport.controllers;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.*;
import ru.fcpsr.domainsport.enums.Access;
import ru.fcpsr.domainsport.enums.Permission;
import ru.fcpsr.domainsport.enums.Role;
import ru.fcpsr.domainsport.models.AppUser;
import ru.fcpsr.domainsport.models.AuthToken;
import ru.fcpsr.domainsport.services.*;
import ru.fcpsr.domainsport.utils.StringGenerator;

import java.util.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/principal")
public class PrincipalController {

    private final AccessService accessService;
    private final UserService userService;
    private final EmailService emailService;
    private final AuthTokenService authTokenService;
    private final SportService sportService;
    private final PasswordEncoder encoder;

    private final List<Role> workRoles = new ArrayList<>(Arrays.asList(Role.MODERATOR,Role.MANAGER,Role.WORKER));

    @GetMapping("/profile")
    @PreAuthorize("@AccessService.isAuthenticate(#authentication)")
    public Mono<Rendering> profilePage(@AuthenticationPrincipal Authentication authentication){
        AppUser appUser = (AppUser) authentication.getPrincipal();
        return userService.getUserById(appUser.getId()).flatMap(user -> {
            log.info("user {} found", user.getFullName());
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Profile page")
                            .modelAttribute("index","profile-page")
                            .modelAttribute("appUser", accessService.getCompletedUser(user))
                            .modelAttribute("passwordForm",new PasswordDTO())
                            .modelAttribute("message", new MailMessage())
                            .modelAttribute("roleList", workRoles)
                            .modelAttribute("accessList", Access.values())
                            .build()
            );
        });
    }

    @PostMapping("/mail/send")
    @PreAuthorize("@AccessService.isAdmin(#authentication)")
    public Mono<Rendering> sendMail(@AuthenticationPrincipal Authentication authentication, @ModelAttribute(name = "message") @Valid MailMessage message, Errors errors){
        log.info("Attention! Incoming message [{}].", message);
        return userService.checkEmail(message.getMail()).flatMap(exist -> {
            if(exist){
                log.info("mail {} exist in data base!", message.getMail());
                errors.rejectValue("mail","","Такой e-mail уже зарегистрирован");
            }
            if(errors.hasErrors()){
                AppUser appUser = (AppUser) authentication.getPrincipal();
                return userService.getUserById(appUser.getId()).flatMap(user -> {
                    message.setSportId(0);
                    return Mono.just(
                            Rendering.view("template")
                                    .modelAttribute("title","Profile page")
                                    .modelAttribute("index","profile-page")
                                    .modelAttribute("appUser", accessService.getCompletedUser(user))
                                    .modelAttribute("passwordForm",new PasswordDTO())
                                    .modelAttribute("message", message)
                                    .modelAttribute("roleList", workRoles)
                                    .modelAttribute("accessList", Access.values())
                                    .build()
                    );
                });
            }

            UUID link = UUID.randomUUID();
            MailMessage mailMessage = emailService.getMailMessageForRegistration(message.getMail(),link.toString());
            try {
                emailService.sendEmail(mailMessage);
            } catch (MessagingException e) {
                return Mono.error(new RuntimeException(e)).then(Mono.just(Rendering.redirectTo("/error").build()));
            }
            return Mono.just(new AuthToken()).flatMap(authToken -> {
                authToken.setEmail(message.getMail());
                authToken.setLink(link.toString());
                authToken.setRole(message.getRole());
                authToken.setAccess(message.getAccess());
                authToken.setSportId(message.getSportId());
                return authTokenService.save(authToken);
            }).flatMap(authToken -> {
                log.info("token {} saved", authToken);
                return Mono.just(Rendering.redirectTo("/principal/profile").build());
            });
        });
    }

    @PostMapping("/update/password")
    @PreAuthorize("@AccessService.isAuthenticate(#authentication)")
    public Mono<Rendering> updatePassword(@AuthenticationPrincipal Authentication authentication, @ModelAttribute(name = "passwordForm") @Valid PasswordDTO passwordDTO, Errors errors){
        AppUser appUser = (AppUser) authentication.getPrincipal();
        return userService.checkPassword(appUser.getId(), passwordDTO.getOldPassword()).flatMap(good -> {
            if(!good){
                errors.rejectValue("oldPassword","","Пароль не совпадает, попробуйте еще раз");
            }
            if(!passwordDTO.getPassword().equals(passwordDTO.getConfirmPassword())){
                errors.rejectValue("confirmPassword","","Подтверждение пароля не совпадает с новым паролем");
            }
            if(errors.hasErrors()){
                return userService.getUserById(appUser.getId()).flatMap(user -> Mono.just(
                        Rendering.view("template")
                                .modelAttribute("title","Profile page")
                                .modelAttribute("index","profile-page")
                                .modelAttribute("appUser", accessService.getCompletedUser(user))
                                .modelAttribute("passwordForm",passwordDTO)
                                .modelAttribute("message", new MailMessage())
                                .modelAttribute("roleList", workRoles)
                                .build()
                ));
            }

            return userService.updatePassword(appUser.getId(),passwordDTO).flatMap(user -> {
                log.info("user password updated. user: {}", user.getFullName());
                return Mono.just(Rendering.redirectTo("/principal/profile").build());
            });
        });
    }

    @PostMapping("/update/profile")
    @PreAuthorize("@AccessService.isAuthenticate(#authentication)")
    public Mono<Rendering> updatePrincipal(@AuthenticationPrincipal Authentication authentication, @ModelAttribute(name = "appUser") @Valid AppUserDTO userDTO, Errors errors){
        AppUser currentUser = (AppUser) authentication.getPrincipal();
        if(currentUser.getId() == userDTO.getId()) {
            return userService.checkUsername(userDTO.getUsername(), userDTO.getId()).flatMap(exist -> {
                if (exist) {
                    log.info("exist: {}", true);
                    errors.rejectValue("username", "", "Такое имя пользователя уже занято, придумайте другое");
                }
                if (errors.hasFieldErrors("username") || errors.hasFieldErrors("firstname") || errors.hasFieldErrors("lastname") || errors.hasFieldErrors("birthday")) {
                    return accessService.getAllRoleAccessDTO(userDTO.getRoleAccessIds()).collectList().flatMap(list -> {
                        if(list.size() != 0){
                            userDTO.setRoleAccessList(list);
                        }
                        return Mono.just(
                                Rendering.view("template")
                                        .modelAttribute("title", "Profile page")
                                        .modelAttribute("index", "profile-page")
                                        .modelAttribute("appUser", userDTO)
                                        .modelAttribute("passwordForm",new PasswordDTO())
                                        .modelAttribute("message", new MailMessage())
                                        .modelAttribute("roleList", workRoles)
                                        .build()
                        );
                    });
                }

                return userService.baseUserUpdate(userDTO).flatMap(user -> {
                    log.info("user {} updated", user.getFullName());
                    return Mono.just(Rendering.redirectTo("/principal/profile").build());
                });
            });
        }else{
            return Mono.just(Rendering.redirectTo("/error").build());
        }
    }

    @GetMapping("/registration/{link}")
    public Mono<Rendering> registrationPage(@PathVariable String link){
        return authTokenService.getToken(link).flatMap(token -> {
            if(token.getId() != 0 && !token.isStatus()){
                AppUserDTO appUserDTO = new AppUserDTO();
                appUserDTO.setRole(token.getRole());
                RoleAccessDTO roleAccessDTO = new RoleAccessDTO();
                roleAccessDTO.setAccess(token.getAccess());
                roleAccessDTO.setPermissionList(getPermissions(token.getRole()));
                return sportService.getById(token.getSportId()).flatMap(sport -> {
                    SportDTO sportDTO = new SportDTO(sport);
                    roleAccessDTO.setSport(sportDTO);
                    appUserDTO.setRoleAccessList(new ArrayList<>(List.of(roleAccessDTO)));
                    return Mono.just(
                            Rendering.view("template")
                                    .modelAttribute("title","Registration page")
                                    .modelAttribute("index","registration-page")
                                    .modelAttribute("principalDTO", appUserDTO)
                                    .modelAttribute("uuid",link)
                                    .build()
                    );
                }).switchIfEmpty(Mono.just(appUserDTO).flatMap(u -> {
                    u.setRoleAccessList(new ArrayList<>(List.of(roleAccessDTO)));
                    return Mono.just(
                            Rendering.view("template")
                                    .modelAttribute("title","Registration page")
                                    .modelAttribute("index","registration-page")
                                    .modelAttribute("principalDTO", u)
                                    .modelAttribute("uuid",link)
                                    .build()
                    );
                }));
            }else{
                return Mono.just(Rendering.redirectTo("/error").build());
            }
        });
    }

    @PostMapping("/registration/{link}")
    public Mono<Rendering> registrationCompleted(@PathVariable String link, @ModelAttribute(name = "principalDTO") @Valid AppUserDTO userDTO, Errors errors){
        return authTokenService.getToken(link).flatMap(token -> {
            if(token.getId() != 0 && !token.isStatus()){
                if(!token.getEmail().equals(userDTO.getEmail())){
                    errors.rejectValue("email","","Почтовый ящик не совпадает");
                }
                return userService.checkUsername(userDTO.getUsername()).flatMap(exist -> {
                    if(exist){
                        errors.rejectValue("username","","Такой псевдоним уже занят");
                    }
                    if(errors.hasErrors()){
                        userDTO.setRole(token.getRole());
                        RoleAccessDTO roleAccessDTO = new RoleAccessDTO();
                        roleAccessDTO.setPermissionList(getPermissions(token.getRole()));
                        return sportService.getById(token.getSportId()).flatMap(sport -> {
                            SportDTO sportDTO = new SportDTO(sport);
                            roleAccessDTO.setSport(sportDTO);
                            userDTO.setRoleAccessList(new ArrayList<>(List.of(roleAccessDTO)));
                            return Mono.just(
                                    Rendering.view("template")
                                            .modelAttribute("title","Registration page")
                                            .modelAttribute("index","registration-page")
                                            .modelAttribute("principalDTO", userDTO)
                                            .modelAttribute("uuid",link)
                                            .build()
                            );
                        }).switchIfEmpty(Mono.just(userDTO).flatMap(u -> {
                            u.setRoleAccessList(new ArrayList<>(List.of(roleAccessDTO)));
                            return Mono.just(
                                    Rendering.view("template")
                                            .modelAttribute("title","Registration page")
                                            .modelAttribute("index","registration-page")
                                            .modelAttribute("principalDTO", u)
                                            .modelAttribute("uuid",link)
                                            .build()
                            );
                        }));
                    }

                    /*
                    1. Создаем для пользователя пароль
                    2. Создаем и сохраняем пользователя
                    3. Меняем статус токена
                    4. Отправляем по почте пароль пользователю
                    5. Перенаправляем пользователя на страницу с сообщением об отправленном пароле
                     */

                    String randomPassword = StringGenerator.generatePassword(14);
                    return Mono.just(new AppUser(userDTO)).flatMap(user -> {
                        user.setPoliticAccept(true);
                        user.setRole(token.getRole());
                        user.setPassword(encoder.encode(randomPassword));
                        return userService.saveUser(user);
                    }).flatMap(user -> {
                        log.info("pre-saved {}", user);
                        return accessService.createAccess(token,user.getId(),getPermissions(token.getRole())).flatMap(roleAccess -> {
                            Set<Long> ids = new HashSet<>();
                            ids.add(roleAccess.getId());
                            user.setRoleAccessIds(ids);
                            return userService.saveUser(user);
                        });
                    }).flatMap(user -> {
                        log.info("{} completed saved", user);
                        return authTokenService.tokenOff(token);
                    }).flatMap(tokenOff -> {
                        log.info("registration token if off {}", tokenOff);
                        try {
                            emailService.sendEmail(emailService.getMailMessageForPassword(tokenOff.getEmail(),randomPassword));
                        } catch (MessagingException e) {
                            return Mono.error(new RuntimeException(e)).then(Mono.just(Rendering.redirectTo("/error").build()));
                        }
                        return Mono.just(
                                Rendering.view("template")
                                        .modelAttribute("title","Reg completed")
                                        .modelAttribute("index", "information-page")
                                        .build()
                        );
                    });
                });
            }else{
                return Mono.just(Rendering.redirectTo("/error").build());
            }
        });
    }

    private List<Permission> getPermissions(Role role){
        if(role.equals(Role.ADMIN)){
            return new ArrayList<>(List.of(Permission.values()));
        }else if(role.equals(Role.MODERATOR)){
            return new ArrayList<>(List.of(Permission.values()));
        }else if(role.equals(Role.MANAGER)){
            return new ArrayList<>(List.of(Permission.values()));
        }else if(role.equals(Role.WORKER)){
            return new ArrayList<>(List.of(Permission.READ,Permission.CREATE,Permission.UPDATE));
        }else{
            return new ArrayList<>(List.of(Permission.READ));
        }
    }
}
