package ru.fcpsr.domainsport.controllers;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.MailMessage;
import ru.fcpsr.domainsport.enums.Role;
import ru.fcpsr.domainsport.models.AppUser;
import ru.fcpsr.domainsport.services.AccessService;
import ru.fcpsr.domainsport.services.EmailService;
import ru.fcpsr.domainsport.services.UserService;

import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/principal")
public class PrincipalController {

    private final AccessService accessService;
    private final UserService userService;
    private final EmailService emailService;

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
                            .modelAttribute("roleList", Role.values())
                            .build()
            );
        });
    }

    @PostMapping("/mail/send")
    @PreAuthorize("@AccessService.isAdmin(#authentication)")
    public Mono<Rendering> sendMail(@AuthenticationPrincipal Authentication authentication, @ModelAttribute(name = "message") MailMessage message){
        message.setTitle("Приглашение для регистрации в Домен Спорт");
        message.setMessage(getHtmlMessage(message));
        try {
            emailService.sendEmail(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return Mono.just(Rendering.redirectTo("/").build());
    }

    private String getHtmlMessage(MailMessage mailMessage){
        UUID uuid = UUID.randomUUID();
        String message = "" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Ваше письмо</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "        }\n" +
                "        .container {\n" +
                "            width: 80%;\n" +
                "            margin: auto;\n" +
                "        }\n" +
                "        .header {\n" +
                "            background-color: #f8f9fa;\n" +
                "            padding: 20px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .content {\n" +
                "            margin-top: 20px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>Приветствую вас!</h1>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <h2>" + mailMessage.getTitle() + "</h2>\n" +
                "            <p>Ваша ссылка на регистрацию:</p>\n" +
                "            <p>http://localhost:8080/principal/registration/" + uuid + "</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
        return message;
    }
}
