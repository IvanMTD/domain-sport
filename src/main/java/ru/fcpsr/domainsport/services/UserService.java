package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.models.AppUser;
import ru.fcpsr.domainsport.repositories.AppUserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements ReactiveUserDetailsService{

    private final AppUserRepository userRepository;

    public Mono<AppUser> getUserByUsername(String username){
        return userRepository.findAppUserByUsername(username);
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Mono<AppUser> getUserByOauthId(Object psuid) {
        return userRepository.findByOauthId(psuid);
    }

    public Mono<AppUser> getUserById(long id) {
        return userRepository.findById(id);
    }

    public Mono<Boolean> checkEmail(String mail) {
        return userRepository.findByEmail(mail).flatMap(user -> {
            AppUser appUser = (AppUser) user;
            log.info("mail {} found", appUser.getEmail());
            return Mono.just(true);
        }).switchIfEmpty(Mono.just(false));
    }

    public Mono<Boolean> checkUsername(String username) {
        return userRepository.findByUsername(username).flatMap(userDetails -> Mono.just(true)).defaultIfEmpty(false);
    }

    public Mono<AppUser> saveUser(AppUser user) {
        return userRepository.save(user);
    }
}
