package ru.fcpsr.domainsport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.fcpsr.domainsport.enums.Season;
import ru.fcpsr.domainsport.enums.SportStatus;
import ru.fcpsr.domainsport.models.Discipline;
import ru.fcpsr.domainsport.models.Sport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class SportDTO {
    private long id;
    private String title;
    private String description;
    private Season season;
    private SportStatus sportStatus;
    private List<DisciplineDTO> disciplines = new ArrayList<>();

    public SportDTO(Sport sport) {
        setId(sport.getId());
        setTitle(sport.getTitle());
        setDescription(sport.getDescription());
        setSeason(sport.getSeason());
        setSportStatus(sport.getSportStatus());
    }
}
