package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import ru.fcpsr.domainsport.enums.Season;
import ru.fcpsr.domainsport.enums.SportStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class Sport {
    @Id
    private long id;

    private String title;
    private String description;
    private Season season;
    private SportStatus sportStatus;
    private Set<Long> disciplineIds = new HashSet<>();

    public void addDiscipline(Discipline discipline) {
        if(disciplineIds == null){
            disciplineIds = new HashSet<>();
        }
        disciplineIds.add(discipline.getId());
    }

    public void setDisciplines(List<Discipline> disciplines) {
        if(disciplineIds == null){
            disciplineIds = new HashSet<>();
        }
        for(Discipline discipline : disciplines){
            disciplineIds.add(discipline.getId());
        }
    }
}
