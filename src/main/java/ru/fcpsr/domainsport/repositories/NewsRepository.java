package ru.fcpsr.domainsport.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.fcpsr.domainsport.models.News;

public interface NewsRepository extends ReactiveCrudRepository<News,Long> {
    Flux<News> findAllByOrderById();
    Flux<News> findAllByOrderById(Pageable pageable);
}
