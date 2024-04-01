package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.models.AuthToken;
import ru.fcpsr.domainsport.repositories.AuthTokenRepository;

@Service
@RequiredArgsConstructor
public class AuthTokenService {
    private final AuthTokenRepository authTokenRepository;

    public Mono<AuthToken> save(AuthToken authToken) {
        return authTokenRepository.save(authToken);
    }

    public Mono<Boolean> checkToken(String token) {
        return authTokenRepository.findByLink(token).flatMap(authToken -> Mono.just(true)).defaultIfEmpty(false);
    }

    public Mono<AuthToken> getToken(String token) {
        return authTokenRepository.findByLink(token).defaultIfEmpty(new AuthToken());
    }

    public Mono<AuthToken> tokenOff(AuthToken token) {
        return authTokenRepository.findByLink(token.getLink()).flatMap(t -> {
            t.setStatus(true);
            return authTokenRepository.save(t);
        });
    }
}
