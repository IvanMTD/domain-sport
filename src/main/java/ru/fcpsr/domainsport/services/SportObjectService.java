package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.models.SportObject;
import ru.fcpsr.domainsport.repositories.SportObjectRepository;

import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SportObjectService {
    private final SportObjectRepository sportObjectRepository;

    // CREATE
    // READ-ALL
    @Cacheable("sportObjects")
    public Flux<SportObject> getAllSortedById(Pageable pageable) {
        return sportObjectRepository.findAllBy(pageable).collectList().flatMapMany(sol -> {
            sol = sol.stream().sorted(Comparator.comparing(SportObject::getId)).collect(Collectors.toList());
            return Flux.fromIterable(sol);
        }).flatMapSequential(Mono::just);
    }
    // READ-ONE
    @Cacheable("sportObjects")
    public Mono<SportObject> getObjectById(long objectId) {
        return sportObjectRepository.findById(objectId);
    }
    // UPDATE
    // DELETE
    // COUNT
}
