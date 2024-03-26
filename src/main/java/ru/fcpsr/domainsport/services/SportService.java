package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.models.Discipline;
import ru.fcpsr.domainsport.models.Sport;
import ru.fcpsr.domainsport.repositories.SportRepository;

import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SportService {
    private final SportRepository repository;

    public Flux<Sport> getAllByTitleLike(String query) {
        return repository.findSportsWithPartOfTitle("%" + query + "%");
    }

    public Mono<Sport> getById(long sportId) {
        return repository.findById(sportId);
    }

    public Mono<Sport> save(Sport sport) {
        return repository.save(sport);
    }

    public Mono<Sport> update(Discipline discipline) {
        return repository.findById(discipline.getSportId()).flatMap(sport -> {
            sport.addDiscipline(discipline);
            return repository.save(sport);
        });
    }
}
