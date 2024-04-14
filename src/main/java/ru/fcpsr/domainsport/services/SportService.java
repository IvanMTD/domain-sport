package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.models.Discipline;
import ru.fcpsr.domainsport.models.Sport;
import ru.fcpsr.domainsport.repositories.SportRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SportService {
    private final SportRepository sportRepository;

    // CREATE
    @CacheEvict(value = "sports", allEntries = true)
    public Mono<Sport> save(Sport sport) {
        return sportRepository.save(sport);
    }
    // READ-ALL
    @Cacheable("sports")
    public Flux<Sport> getAllByTitleLike(String query) {
        String[] parts = query.split(" ");
        List<Flux<Sport>> list = new ArrayList<>();
        for(String part : parts){
            String sportNamePart = "%" + part + "%";
            list.add(sportRepository.findAllByTitleLikeIgnoreCase(sportNamePart));
        }
        return Flux.merge(list).distinct();
    }
    // READ-ONE
    @Cacheable("sports")
    public Mono<Sport> getById(long sportId) {
        return sportRepository.findById(sportId);
    }
    //@Cacheable(value = "cacheTitlesSports", key="#title")
    public Mono<Sport> getByTitle(String title) {
        return sportRepository.findByTitle(title).defaultIfEmpty(new Sport());
    }
    // UPDATE
    @CacheEvict(value = "sports", allEntries = true)
    public Mono<Sport> update(Discipline discipline) {
        return sportRepository.findById(discipline.getSportId()).flatMap(sport -> {
            sport.addDiscipline(discipline);
            return sportRepository.save(sport);
        });
    }
    // DELETE
    // COUNT
}
