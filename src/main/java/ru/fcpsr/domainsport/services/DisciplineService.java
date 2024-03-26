package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.fcpsr.domainsport.dto.SportDTO;
import ru.fcpsr.domainsport.models.Discipline;
import ru.fcpsr.domainsport.repositories.DisciplineRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DisciplineService {
    private final DisciplineRepository repository;

    public Flux<Discipline> getAll() {
        return repository.findAll();
    }

    public Flux<Discipline> getAllBySportId(long sportId) {
        return repository.findAllBySportId(sportId);
    }

    public Flux<Discipline> getAllByIds(Set<Long> disciplineIds) {
        return repository.findAllByIdIn(disciplineIds);
    }
}
