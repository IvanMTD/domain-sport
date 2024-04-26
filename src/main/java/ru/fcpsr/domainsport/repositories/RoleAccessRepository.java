package ru.fcpsr.domainsport.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.fcpsr.domainsport.models.RoleAccess;

import java.util.Set;

public interface RoleAccessRepository extends ReactiveCrudRepository<RoleAccess,Long> {
    Flux<RoleAccess> findAllByIdIn(Set<Long> ids);
}
