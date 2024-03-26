package ru.fcpsr.domainsport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.fcpsr.domainsport.models.Discipline;

@Data
@NoArgsConstructor
public class DisciplineDTO {
    private long id;
    private String title;
    private String description;
    private SportDTO sport;

    public DisciplineDTO(Discipline discipline) {
        setId(discipline.getId());
        setTitle(discipline.getTitle());
        setDescription(discipline.getDescription());
    }
}
