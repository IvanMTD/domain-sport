package ru.fcpsr.domainsport.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.fcpsr.domainsport.models.Discipline;

public interface DisciplineRepository extends ReactiveCrudRepository<Discipline,Long> {
}
