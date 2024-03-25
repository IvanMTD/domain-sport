package ru.fcpsr.domainsport.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.models.AppUser;

public interface AppUserRepository extends ReactiveCrudRepository<AppUser,Long> {
    Mono<UserDetails> findByUsername(String username);
    Mono<AppUser> findAppUserByUsername(String username);
    Mono<UserDetails> findByEmail(String email);

    Mono<UserDetails> findByOauthId(String oauthId);
}
