package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
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

    public Flux<EkpDTO> getAll(){
        return repository.findAll().flatMap(ekp -> Mono.just(new EkpDTO(ekp)));
    }

    public Flux<EkpDTO> getAll(Pageable pageable){
        return repository.findAllBy(pageable).flatMap(ekp -> Mono.just(new EkpDTO(ekp)));
    }

    public Flux<EkpDTO> getByDate(String query) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(query,formatter);
        return repository.findAllWitchDate(localDate).flatMap(ekp -> Mono.just(new EkpDTO(ekp)));
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
}
