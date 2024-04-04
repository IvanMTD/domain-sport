package ru.fcpsr.domainsport.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.fcpsr.domainsport.models.SportObject;

public interface SportObjectRepository extends ReactiveCrudRepository<SportObject,Long> {
    Flux<SportObject> findAllBy(Pageable pageable);
}
