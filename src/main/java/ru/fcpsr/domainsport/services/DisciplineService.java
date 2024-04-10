package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.fcpsr.domainsport.models.Discipline;
import ru.fcpsr.domainsport.repositories.DisciplineRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DisciplineService {
    private final DisciplineRepository disciplineRepository;
    // CREATE
    // READ-ALL
    @Cacheable("disciplines")
    public Flux<Discipline> getAll() {
        return disciplineRepository.findAll();
    }
    @Cacheable("disciplines")
    public Flux<Discipline> getAllBySportId(long sportId) {
        return disciplineRepository.findAllBySportId(sportId);
    }
    @Cacheable("disciplines")
    public Flux<Discipline> getAllByIds(Set<Long> disciplineIds) {
        return disciplineRepository.findAllByIdIn(disciplineIds);
    }
    // READ-ONE
    // UPDATE
    // DELETE
    // COUNT
}
