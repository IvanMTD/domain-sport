package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.EkpDTO;
import ru.fcpsr.domainsport.models.Ekp;
import ru.fcpsr.domainsport.repositories.EkpRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EkpService {
    private final EkpRepository ekpRepository;

    // CREATE
    @CacheEvict(value = "ekpList", allEntries = true)
    public Mono<Ekp> save(EkpDTO ekpDTO) {
        return Mono.just(new Ekp(ekpDTO)).flatMap(ekpRepository::save);
    }
    @CacheEvict(value = "ekpList", allEntries = true)
    public Mono<Ekp> save(Ekp ekp) {
        return ekpRepository.save(ekp);
    }
    // READ-ALL
    @Cacheable("ekpList")
    public Flux<Ekp> getAllBySportId(long sportId) {
        return ekpRepository.findAllBySportId(sportId);
    }
    @Cacheable("ekpList")
    public Flux<Ekp> getAllByParam(Pageable pageable, String search){
        if(search.equals("all")) {
            return ekpRepository.findAllByOrderByBeginning(pageable);
        }else{
            long sportId = Long.parseLong(search);
            return ekpRepository.findAllBySportIdOrderByBeginning(pageable, sportId);
        }
    }
    @Cacheable("ekpList")
    public Flux<Ekp> getAll(){
        return ekpRepository.findAll();
    }
    @Cacheable("ekpList")
    public Flux<EkpDTO> getByDate(String query) {
        return getAllWitchDate(dateConverter(query)).flatMap(ekp -> Mono.just(new EkpDTO(ekp)));
    }
    @Cacheable("ekpList")
    public Flux<EkpDTO> getByDate(LocalDate localDate) {
        return getAllWitchDate(localDate).flatMap(ekp -> Mono.just(new EkpDTO(ekp)));
    }
    @Cacheable("ekpList")
    public Flux<Ekp> getAllWitchDate(LocalDate date){
        return ekpRepository.findAllWitchDate(date);
    }
    @Cacheable("ekpList")
    public Flux<Ekp> getAllBySportIdAndDate(long sportId, String date) {
        return ekpRepository.findAllBySportIdAndDate(sportId,dateConverter(date));
    }
    @Cacheable("ekpList")
    public Flux<Ekp> getAllBySportIdAndRangeDates(long id, String begin, String end) {
        return ekpRepository.findAllBySportIdAndDateRange(id,dateConverter(begin),dateConverter(end));
    }
    @Cacheable("ekpList")
    public Flux<Ekp> getAllSortedByCurrentDate() {
        return ekpRepository.findAll().collectList().flatMapMany(ekpList -> {
            ekpList = ekpList.stream().sorted(Comparator.comparing(ekp ->{
                int beginning = ekp.getBeginning().getDayOfYear();
                int ending = ekp.getEnding().getDayOfYear();
                int current = LocalDate.now().getDayOfYear();
                int dif = Math.abs(current - (beginning + (ending - beginning)));
                return dif;
            })).collect(Collectors.toList());
            return Flux.fromIterable(ekpList);
        }).flatMapSequential(Mono::just);
    }
    // READ-ONE
    @Cacheable("ekpList")
    public Mono<Ekp> getById(long eventId) {
        return ekpRepository.findById(eventId);
    }
    // UPDATE
    @CachePut(value = "ekpList", key = "#ekpDTO.id")
    public Mono<Ekp> update(EkpDTO ekpDTO) {
        return ekpRepository.findById(ekpDTO.getId()).flatMap(ekp -> {
            ekp.update(ekpDTO);
            return ekpRepository.save(ekp);
        });
    }
    // DELETE
    @CacheEvict(value = "ekpList", allEntries = true)
    public Mono<Ekp> delete(Ekp ekpOriginal) {
        return ekpRepository.delete(ekpOriginal).then(Mono.just(ekpOriginal));
    }
    // COUNT
    public Mono<Long> getCount(String search){
        if(search.equals("all")) {
            return ekpRepository.count();
        }else{
            long sportId = Long.parseLong(search);
            return ekpRepository.countBySportId(sportId);
        }
    }
    private LocalDate dateConverter(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date,formatter);
    }
}
