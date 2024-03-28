package ru.fcpsr.domainsport.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.models.Ekp;
import ru.fcpsr.domainsport.models.Sport;

public interface SportRepository extends ReactiveCrudRepository<Sport,Long> {
    @Query("SELECT * FROM sport WHERE lower(title) LIKE lower(:query)")
    Flux<Sport> findSportsWithPartOfTitle(@Param("query") String query);

    Mono<Sport> findByTitle(String title);
}
