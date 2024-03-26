package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.models.AppUser;
import ru.fcpsr.domainsport.repositories.AppUserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultReactiveOAuth2UserService {
    private final AppUserRepository repository;
    private final PasswordEncoder encoder;

    @Override
    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        return super.loadUser(userRequest).flatMap(user -> {
            String username = user.getAttribute("login");
            String firstname = user.getAttribute("first_name");
            String lastname = user.getAttribute("last_name");
            String email = user.getAttribute("default_email");
            String birthday = user.getAttribute("birthday");
            String avatarId = user.getAttribute("default_avatar_id");
            String oauthId = user.getAttribute("psuid");

            return repository.findByOauthId(oauthId).flatMap(appUser -> {
                log.info("user found in database: " + appUser.toString());
                log.info("return current user " + user);
                return Mono.just(user);
            }).switchIfEmpty(Mono.just(new AppUser()).flatMap(appUser -> {
                appUser.setUsername(oauthId);
                appUser.setPassword(encoder.encode("Admin2020"));
                appUser.setFirstname(firstname);
                appUser.setLastname(lastname);
                appUser.setEmail(email);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                appUser.setBirthday(LocalDate.parse(birthday,formatter));
                appUser.setPlacedAt(LocalDate.now());
                appUser.setAvatarId(avatarId);
                appUser.setOauthId(oauthId);
                return repository.save(appUser).flatMap(savedUser -> {
                    log.info("new user saved in data base: " + savedUser.toString());
                    return Mono.just(user);
                });
            }));
        });
    }
}
