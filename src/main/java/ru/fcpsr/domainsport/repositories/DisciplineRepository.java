package ru.fcpsr.domainsport.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.fcpsr.domainsport.models.Discipline;

import java.util.Set;

public interface DisciplineRepository extends ReactiveCrudRepository<Discipline,Long> {
    Flux<Discipline> findAllBySportId(long sportId);

    Flux<Discipline> findAllByIdIn(Set<Long> disciplineIds);
}
