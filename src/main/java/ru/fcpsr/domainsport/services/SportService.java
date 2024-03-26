package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.fcpsr.domainsport.models.Sport;
import ru.fcpsr.domainsport.repositories.SportRepository;

@Service
@RequiredArgsConstructor
public class SportService {
    private final SportRepository repository;

    public Flux<Sport> getAllByTitleLike(String query) {
        return repository.findSportsWithPartOfTitle("%" + query + "%");
    }
}
