package ru.fcpsr.domainsport.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.models.School;

public interface SchoolRepository extends ReactiveCrudRepository<School,Long> {
    Flux<School> findAllByOrderByIdDesc(Pageable pageable);
    Flux<School> findAllByOrderByIdDesc();
    Flux<School> findAllBySubjectLikeIgnoreCase(String query);
    Flux<School> findAllBySubjectLikeIgnoreCase(Pageable pageable, String query);
    Mono<Long> countBySubject(String subject);
}
