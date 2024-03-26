package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.models.AppUser;
import ru.fcpsr.domainsport.repositories.AppUserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements ReactiveUserDetailsService{

    private final AppUserRepository repository;

    public Mono<AppUser> getUserByUsername(String username){
        return repository.findAppUserByUsername(username);
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return repository.findByUsername(username).flatMap(user -> {
            log.info("found user in data base: " + user.toString());
            return Mono.just(user);
        });
    }

    public Mono<AppUser> getUserByOauthId(Object psuid) {
        return repository.findByOauthId(psuid);
    }
}
