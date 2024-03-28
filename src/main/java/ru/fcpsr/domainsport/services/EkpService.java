package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.EkpDTO;
import ru.fcpsr.domainsport.models.Ekp;
import ru.fcpsr.domainsport.repositories.EkpRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EkpService {
    private final EkpRepository repository;

    public Flux<EkpDTO> getAllDTO(){
        return repository.findAll().flatMap(ekp -> Mono.just(new EkpDTO(ekp)));
    }

    public Flux<EkpDTO> getAllDTO(Pageable pageable){
        return repository.findAllBy(pageable).flatMap(ekp -> Mono.just(new EkpDTO(ekp)));
    }

    public Flux<Ekp> getAll(){
        return repository.findAll();
    }

    public Flux<EkpDTO> getByDate(String query) {
        return repository.findAllWitchDate(dateConverter(query)).flatMap(ekp -> Mono.just(new EkpDTO(ekp)));
    }

    public Flux<EkpDTO> getByDate(LocalDate localDate) {
        return repository.findAllWitchDate(localDate).flatMap(ekp -> Mono.just(new EkpDTO(ekp)));
    }

    public Mono<Long> getCount(){
        return repository.count();
    }

    public Mono<Ekp> save(EkpDTO ekpDTO) {
        return Mono.just(new Ekp(ekpDTO)).flatMap(ekp -> repository.save(ekp));
    }

    public Mono<Ekp> save(Ekp ekp) {
        return repository.save(ekp);
    }

    public Flux<Ekp> getAllBySportId(long sportId) {
        return repository.findAllBySportId(sportId);
    }

    public Mono<Ekp> getById(long eventId) {
        return repository.findById(eventId);
    }

    public Mono<Ekp> update(EkpDTO ekpDTO) {
        return repository.findById(ekpDTO.getId()).flatMap(ekp -> {
            ekp.update(ekpDTO);
            return repository.save(ekp);
        });
    }

    public Mono<Ekp> delete(Ekp ekpOriginal) {
        return repository.delete(ekpOriginal).then(Mono.just(ekpOriginal));
    }

    public Flux<Ekp> getAllBySportIdAndDate(long sportId, String date) {
        return repository.findAllBySportIdAndDate(sportId,dateConverter(date));
    }

    public Flux<Ekp> getAllBySportIdAndRangeDates(long id, String begin, String end) {
        return repository.findAllBySportIdAndDateRange(id,dateConverter(begin),dateConverter(end));
    }

    private LocalDate dateConverter(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date,formatter);
    }
}
