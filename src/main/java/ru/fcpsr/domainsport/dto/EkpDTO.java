package ru.fcpsr.domainsport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.codec.multipart.FilePart;
import ru.fcpsr.domainsport.enums.Status;
import ru.fcpsr.domainsport.models.Ekp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class EkpDTO {
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
    private long sportId;
    private List<Long> disciplineIds = new ArrayList<>();
    private FilePart logo;
    private FilePart image;
    private float s;
    private float d;
    private String present;

    public EkpDTO(Ekp ekp){
        setId(ekp.getId());
        setNum(ekp.getNum());
        setTitle(ekp.getTitle());
        setBeginning(ekp.getBeginning());
        setEnding(ekp.getEnding());
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
