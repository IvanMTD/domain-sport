package ru.fcpsr.domainsport.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.fcpsr.domainsport.models.AuthToken;

public interface AuthTokenRepository extends ReactiveCrudRepository<AuthToken,Long> {
}
