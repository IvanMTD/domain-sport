package ru.fcpsr.domainsport.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.models.Ekp;

import java.time.LocalDate;

public interface EkpRepository extends ReactiveCrudRepository<Ekp,Long> {

    Flux<Ekp> findAllBy(Pageable pageable);

    @Query("SELECT * FROM ekp WHERE :date BETWEEN ekp.beginning AND ekp.ending")
    Flux<Ekp> findAllWitchDate(@Param("date") LocalDate date);

    Flux<Ekp> findAllBySportId(long sportId);
    Flux<Ekp> findAllBySportId(Pageable pageable, long sportId);

    @Query("SELECT * FROM ekp WHERE :sportId = ekp.sport_id AND :date BETWEEN ekp.beginning AND ekp.ending")
    Flux<Ekp> findAllBySportIdAndDate(@Param("sportId") long sportId, @Param("date") LocalDate date);

    @Query("SELECT * FROM ekp WHERE ekp.sport_id = :sportId AND ((beginning >= :begin AND beginning <= :end) OR (ending >= :begin AND ending <= :end))")
    Flux<Ekp> findAllBySportIdAndDateRange(@Param("sportId") long sportId,@Param("begin") LocalDate begin,@Param("end") LocalDate end);

    Mono<Long> countBySportId(long sportId);
}
