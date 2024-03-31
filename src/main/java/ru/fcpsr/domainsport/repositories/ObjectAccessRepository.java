package ru.fcpsr.domainsport.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.models.ObjectAccess;

import java.util.Set;

public interface ObjectAccessRepository extends ReactiveCrudRepository<ObjectAccess,Long> {
    Flux<ObjectAccess> findAllByIdIn(Set<Long> objectAccessIds);
}
