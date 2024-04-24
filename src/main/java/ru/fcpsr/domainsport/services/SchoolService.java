package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.SchoolDTO;
import ru.fcpsr.domainsport.models.School;
import ru.fcpsr.domainsport.repositories.SchoolRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolService {
    private final SchoolRepository schoolRepository;

    public Mono<Long> getCount() {
        return schoolRepository.count();
    }

    @Cacheable(cacheNames = "schools")
    public Flux<School> getAllOrderByIdDesc(Pageable pageable) {
        return schoolRepository.findAllByOrderByIdDesc(pageable);
    }
    public Flux<String> getAllBySubject(String query) {
        String[] parts = query.split(" ");
        List<Flux<School>> schoolFlux = new ArrayList<>();
        for(String part : parts){
            String keyWord = "%" + part + "%";
            schoolFlux.add(schoolRepository.findAllBySubjectLikeIgnoreCase(keyWord));
        }

        return Flux.merge(schoolFlux).distinct().flatMap(school -> {
            String[] subjectParts = school.getSubject().split(" ");
            int check = 0;
            for(String sp : subjectParts){
                for(String p : parts){
                    if(sp.toLowerCase().contains(p.toLowerCase())){
                        check++;
                    }
                }
            }
            if(check >= parts.length){
                return Mono.just(school.getSubject() + "|");
            }else{
                return Mono.empty();
            }
        }).distinct();
    }

    @Cacheable(cacheNames = "schools")
    public Flux<School> getAllBySearch(Pageable pageable, String search) {
        if(search != null){
            if(search.equals("all")){
                return schoolRepository.findAllByOrderByIdDesc(pageable);
            }else{
                String pattern = "%" + search + "%";
                return schoolRepository.findAllBySubjectLikeIgnoreCase(pageable,pattern).distinct();
            }
        }else{
            return Flux.empty();
        }
    }

    public Mono<Long> getCountBySubject(String subject) {
        if(subject.equals("all")){
            return schoolRepository.count();
        }else {
            return schoolRepository.countBySubject(subject).defaultIfEmpty(0L);
        }
    }

    public Mono<School> getById(long schoolId) {
        return schoolRepository.findById(schoolId);
    }

    public Mono<School> createSchool(SchoolDTO schoolDTO) {
        return schoolRepository.save(new School(schoolDTO));
    }

    public Mono<School> save(School lastStepSchool) {
        return schoolRepository.save(lastStepSchool);
    }
}
