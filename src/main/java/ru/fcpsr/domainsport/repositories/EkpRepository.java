package ru.fcpsr.domainsport.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.fcpsr.domainsport.models.Ekp;

import java.time.LocalDate;

public interface EkpRepository extends ReactiveCrudRepository<Ekp,Long> {

    Flux<Ekp> findAllBy(Pageable pageable);

    @Query("SELECT * FROM ekp WHERE :date BETWEEN ekp.beginning AND ekp.ending")
    Flux<Ekp> findAllWitchDate(@Param("date") LocalDate date);
}
