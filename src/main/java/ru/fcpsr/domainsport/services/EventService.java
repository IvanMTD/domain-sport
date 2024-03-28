package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.dto.DisciplineDTO;
import ru.fcpsr.domainsport.dto.EkpDTO;
import ru.fcpsr.domainsport.dto.SportDTO;
import ru.fcpsr.domainsport.models.Ekp;
import ru.fcpsr.domainsport.repositories.DisciplineRepository;
import ru.fcpsr.domainsport.repositories.EkpRepository;
import ru.fcpsr.domainsport.repositories.SportRepository;

import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EkpRepository ekpRepository;
    private final SportRepository sportRepository;
    private final DisciplineRepository disciplineRepository;

    public Mono<EkpDTO> getEvent(long id){
        return ekpRepository.findById(id).flatMap(ekp -> {
            EkpDTO ekpDTO = new EkpDTO(ekp);
            return sportRepository.findById(ekp.getSportId()).flatMap(sport -> {
                SportDTO sportDTO = new SportDTO(sport);
                return disciplineRepository.findAllById(ekp.getDisciplineIds()).flatMap(discipline -> {
                    DisciplineDTO disciplineDTO = new DisciplineDTO(discipline);
                    return Mono.just(disciplineDTO);
                }).collectList().flatMap(dl -> {
                    dl = dl.stream().sorted(Comparator.comparing(DisciplineDTO::getTitle)).collect(Collectors.toList());
                    sportDTO.setDisciplines(dl);
                    ekpDTO.setSport(sportDTO);
                    return Mono.just(ekpDTO);
                });
            }).flatMap(e -> disciplineRepository.findAllBySportId(ekp.getSportId()).flatMap(discipline -> {
                DisciplineDTO disciplineDTO = new DisciplineDTO(discipline);
                return Mono.just(disciplineDTO);
            }).collectList().flatMap(dl -> {
                dl = dl.stream().sorted(Comparator.comparing(DisciplineDTO::getTitle)).collect(Collectors.toList());
                e.setDisciplines(dl);
                return Mono.just(e);
            }));
        });
    }

    public Mono<EkpDTO> completedEvent(Ekp ekp) {
        return sportRepository.findById(ekp.getSportId()).flatMap(sport -> {
            EkpDTO ekpDTO = new EkpDTO(ekp);
            SportDTO sportDTO = new SportDTO(sport);
            return disciplineRepository.findAllById(ekp.getDisciplineIds()).flatMap(discipline -> {
                DisciplineDTO disciplineDTO = new DisciplineDTO(discipline);
                return Mono.just(disciplineDTO);
            }).collectList().flatMap(dl -> {
                dl = dl.stream().sorted(Comparator.comparing(DisciplineDTO::getTitle)).collect(Collectors.toList());
                sportDTO.setDisciplines(dl);
                ekpDTO.setSport(sportDTO);
                return Mono.just(ekpDTO);
            });
        }).flatMap(e -> disciplineRepository.findAllBySportId(ekp.getSportId()).flatMap(discipline -> {
            DisciplineDTO disciplineDTO = new DisciplineDTO(discipline);
            return Mono.just(disciplineDTO);
        }).collectList().flatMap(dl -> {
            dl = dl.stream().sorted(Comparator.comparing(DisciplineDTO::getTitle)).collect(Collectors.toList());
            e.setDisciplines(dl);
            return Mono.just(e);
        }));
    }
}
