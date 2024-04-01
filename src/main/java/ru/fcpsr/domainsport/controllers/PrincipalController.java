package ru.fcpsr.domainsport.controllers;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.AppUserDTO;
import ru.fcpsr.domainsport.dto.MailMessage;
import ru.fcpsr.domainsport.dto.RoleAccessDTO;
import ru.fcpsr.domainsport.dto.SportDTO;
import ru.fcpsr.domainsport.enums.Permission;
import ru.fcpsr.domainsport.enums.Role;
import ru.fcpsr.domainsport.models.AppUser;
import ru.fcpsr.domainsport.models.AuthToken;
import ru.fcpsr.domainsport.services.*;
import ru.fcpsr.domainsport.utils.StringGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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

    private final List<Role> workRoles = new ArrayList<>(Arrays.asList(Role.MANAGER,Role.WORKER));

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
                            .modelAttribute("message", new MailMessage())
                            .modelAttribute("roleList", workRoles)
                            .build()
            );
        });
    }

    @PostMapping("/mail/send")
    @PreAuthorize("@AccessService.isAdmin(#authentication)")
    public Mono<Rendering> sendMail(@AuthenticationPrincipal Authentication authentication, @ModelAttribute(name = "message") @Valid MailMessage message, Errors errors){
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
                                    .modelAttribute("message", message)
                                    .modelAttribute("roleList", workRoles)
                                    .build()
                    );
                });
            }

            UUID link = UUID.randomUUID();
            message.setTitle("Приглашение для регистрации в Домен Спорт");
            message.setMessage(getHtmlMessage(message, link));
            try {
                emailService.sendEmail(message);
            } catch (MessagingException e) {
                return Mono.error(new RuntimeException(e)).then(Mono.just(Rendering.redirectTo("/error").build()));
            }
            return Mono.just(new AuthToken()).flatMap(authToken -> {
                authToken.setEmail(message.getMail());
                authToken.setLink(link.toString());
                authToken.setRole(message.getRole());
                authToken.setSportId(message.getSportId());
                return authTokenService.save(authToken);
            }).flatMap(authToken -> {
                log.info("token {} saved", authToken);
                return Mono.just(Rendering.redirectTo("/principal/profile").build());
            });
        });
    }

    @PostMapping("/update")
    @PreAuthorize("@AccessService.isAuthenticate(#authentication)")
    public Mono<Rendering> updatePrincipal(@AuthenticationPrincipal Authentication authentication, @ModelAttribute(name = "appUser") @Valid AppUserDTO userDTO, Errors errors){
        System.out.println(userDTO);
        AppUser currentUser = (AppUser) authentication.getPrincipal();
        if(currentUser.getId() == userDTO.getId()) {
            return userService.checkUsername(userDTO.getUsername()).flatMap(exist -> {
                if (exist) {
                    System.out.println("exist");
                    errors.rejectValue("username", "", "Такое имя пользователя уже занято, придумайте другое");
                }
                if (errors.hasErrors()) {
                    System.out.println("ERROR");
                    Mono<AppUserDTO> userMono = userService.getUserById(userDTO.getId()).flatMap(accessService::getCompletedUser);
                    return Mono.just(
                            Rendering.view("template")
                                    .modelAttribute("title", "Profile page")
                                    .modelAttribute("index", "profile-page")
                                    .modelAttribute("appUser", userMono.flatMap(u -> {
                                        userDTO.setEmail(u.getEmail());
                                        userDTO.setRole(u.getRole());
                                        userDTO.setRoleAccess(u.getRoleAccess());
                                        System.out.println(userDTO);
                                        return Mono.just(userDTO);
                                    }))
                                    .modelAttribute("message", new MailMessage())
                                    .modelAttribute("roleList", workRoles)
                                    .build()
                    );
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
                roleAccessDTO.setPermissionList(getPermissions(token.getRole()));
                return sportService.getById(token.getSportId()).flatMap(sport -> {
                    SportDTO sportDTO = new SportDTO(sport);
                    roleAccessDTO.setSport(sportDTO);
                    appUserDTO.setRoleAccess(roleAccessDTO);
                    return Mono.just(
                            Rendering.view("template")
                                    .modelAttribute("title","Registration page")
                                    .modelAttribute("index","registration-page")
                                    .modelAttribute("principalDTO", appUserDTO)
                                    .modelAttribute("uuid",link)
                                    .build()
                    );
                });
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
                            userDTO.setRoleAccess(roleAccessDTO);
                            return Mono.just(
                                    Rendering.view("template")
                                            .modelAttribute("title","Registration page")
                                            .modelAttribute("index","registration-page")
                                            .modelAttribute("principalDTO", userDTO)
                                            .modelAttribute("uuid",link)
                                            .build()
                            );
                        });
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
                        user.setRole(token.getRole());
                        user.setPassword(encoder.encode(randomPassword));
                        return userService.saveUser(user);
                    }).flatMap(user -> {
                        log.info("pre-saved {}", user);
                        return accessService.createAccess(token,user.getId(),getPermissions(token.getRole())).flatMap(roleAccess -> {
                            user.setRoleAccessId(roleAccess.getId());
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
        }else if(role.equals(Role.MANAGER)){
            return new ArrayList<>(List.of(Permission.values()));
        }else if(role.equals(Role.WORKER)){
            return new ArrayList<>(List.of(Permission.READ,Permission.CREATE,Permission.UPDATE));
        }else{
            return new ArrayList<>(List.of(Permission.READ));
        }
    }

    private String getHtmlMessage(MailMessage mailMessage, UUID uuid){
        String message = "" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                "    <title>Приглашение</title>\n" +
                "    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH\" crossorigin=\"anonymous\">" +
                "</head>\n" +
                "<body>\n" +
                "   <div class=\"container\">\n" +
                "     <div class=\"row\">\n" +
                "       <div class=\"col d-flex align-items-center\" style=\"height: 500px\">\n" +
                "         <div class=\"card mx-auto\" style=\"width: 18rem;\">\n" +
                "           <img src=\"https://domensport.ru/img/logo.png\" class=\"card-img-top p-2\" alt=\"...\">\n" +
                "           <div class=\"card-body\">\n" +
                "             <h5 class=\"card-title\">Приглашаем Вас и вашу Федерацию</h5>\n" +
                "             <p class=\"card-text\">Описание приглашения. Надо будет продумать.</p>\n" +
                "             <hr>\n" +
                "             <a href=\"http://localhost:8080/principal/registration/" + uuid + "\" class=\"btn btn-outline-secondary\">Регистрация</a>\n" +
                "           </div>\n" +
                "         </div>\n" +
                "       </div>\n" +
                "     </div>\n" +
                "   </div>" +
                "   <script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js\" integrity=\"sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz\" crossorigin=\"anonymous\"></script>" +
                "</body>\n" +
                "</html>";
        return message;
    }
}
