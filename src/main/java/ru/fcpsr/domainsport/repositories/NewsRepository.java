package ru.fcpsr.domainsport.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.fcpsr.domainsport.models.News;

public interface NewsRepository extends ReactiveCrudRepository<News,Long> {
    Flux<News> findAllByOrderByIdDesc();
    Flux<News> findAllByOrderByIdDesc(Pageable pageable);
    Flux<News> findAllByTitleLikeIgnoreCase(String title);
    Flux<News> findAllByAnnotationLikeIgnoreCase(String title);
    Flux<News> findAllByContentLikeIgnoreCase(String title);
}
