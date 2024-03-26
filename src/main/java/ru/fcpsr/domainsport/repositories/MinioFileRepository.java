package ru.fcpsr.domainsport.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.fcpsr.domainsport.models.MinioFile;

import java.util.Set;

public interface MinioFileRepository extends ReactiveCrudRepository<MinioFile, Integer> {
    Flux<MinioFile> findAllByIdIn(Set<Integer> ids);
}
