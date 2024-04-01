package ru.fcpsr.domainsport.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.models.AuthToken;

public interface AuthTokenRepository extends ReactiveCrudRepository<AuthToken,Long> {
    Mono<AuthToken> findByLink(String token);
}
