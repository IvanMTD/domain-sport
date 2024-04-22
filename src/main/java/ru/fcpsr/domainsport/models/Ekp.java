package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.format.annotation.DateTimeFormat;
import ru.fcpsr.domainsport.dto.EkpDTO;
import ru.fcpsr.domainsport.enums.Status;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Table(name = "ekp")
@NoArgsConstructor
public class Ekp {
    @Id
    private long id;

    private String ekp;
    private String num;
    private String title;
    private String description;
    private Status status;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginning;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ending;
    private String category;
    private String location;
    private String organization;
    private long sportId;
    private Set<Long> disciplineIds = new HashSet<>();
    private long logo;
    private long image;
    private float s;
    private float d;

    public Ekp(EkpDTO ekpDTO) {
        setEkp(ekpDTO.getEkp());
        setNum(ekpDTO.getNum());
        setTitle(ekpDTO.getTitle());
        setDescription(ekpDTO.getDescription());
        setStatus(ekpDTO.getStatus());
        setBeginning(ekpDTO.getBeginning());
        setEnding(ekpDTO.getEnding());
        setCategory(ekpDTO.getCategory());
        setLocation(ekpDTO.getLocation());
        setOrganization(ekpDTO.getOrganization());
        setSportId(ekpDTO.getSportId());
        setS(ekpDTO.getS());
        setD(ekpDTO.getD());
        this.disciplineIds.addAll(ekpDTO.getDisciplineIds());
    }

    public void update(EkpDTO ekpDTO) {
        setEkp(ekpDTO.getEkp());
        setNum(ekpDTO.getNum());
        setTitle(ekpDTO.getTitle());
        setDescription(ekpDTO.getDescription());
        setStatus(ekpDTO.getStatus());
        setBeginning(ekpDTO.getBeginning());
        setEnding(ekpDTO.getEnding());
        setCategory(ekpDTO.getCategory());
        setLocation(ekpDTO.getLocation());
        setOrganization(ekpDTO.getOrganization());
        setSportId(ekpDTO.getSportId());
        setS(ekpDTO.getS());
        setD(ekpDTO.getD());
        this.disciplineIds = new HashSet<>();
        this.disciplineIds.addAll(ekpDTO.getDisciplineIds());
    }
}
