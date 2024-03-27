package ru.fcpsr.domainsport.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.codec.multipart.FilePart;
import ru.fcpsr.domainsport.enums.Status;
import ru.fcpsr.domainsport.models.Ekp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class EkpDTO {
    private long id;

    private String ekp;
    private String num;
    @NotBlank(message = "Укажите название спортивного мероприятия")
    private String title;
    private String description;
    private Status status;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Укажите дату начала мероприятия")
    private LocalDate beginning;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Укажите дату завершения мероприятия")
    private LocalDate ending;
    private String category;
    @NotBlank(message = "Если не знаете полного адреса, укажите город проведения мероприятия")
    private String location;
    @Min(value = 1, message = "Укажите вид спорта")
    private long sportId;
    private List<Long> disciplineIds = new ArrayList<>();
    private FilePart logo;
    private FilePart image;
    private float s;
    private float d;
    private String present;

    public EkpDTO(Ekp ekp){
        setId(ekp.getId());
        setEkp(ekp.getEkp());
        setNum(ekp.getNum());
        setTitle(ekp.getTitle());
        setDescription(ekp.getDescription());
        setStatus(ekp.getStatus());
        setBeginning(ekp.getBeginning());
        setEnding(ekp.getEnding());
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
