package ru.fcpsr.domainsport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.fcpsr.domainsport.models.Ekp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
public class EkpDTO {
    private long id;

    private int num;
    private String title;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginning;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ending;
    private String access;
    private String category;
    private String location;
    private String ekp;
    private float s;
    private float d;
    private String present;

    public EkpDTO(Ekp ekp){
        setId(ekp.getId());
        setNum(ekp.getNum());
        setTitle(ekp.getTitle());
        setBeginning(ekp.getBeginning());
        setEnding(ekp.getEnding());
        setAccess(ekp.getAccess());
        setCategory(ekp.getCategory());
        setLocation(ekp.getLocation());
        setEkp(ekp.getEkp());
        setS(ekp.getS());
        setD(ekp.getD());
        setPresent("islands#blueGovernmentIcon");
    }

    public String getBeginningDate(){
        return DateTimeFormatter.ofPattern("dd.MM.yyyy").format(beginning);
    }
    public String getEndingDate(){
        return DateTimeFormatter.ofPattern("dd.MM.yyyy").format(ending);
    }
}
