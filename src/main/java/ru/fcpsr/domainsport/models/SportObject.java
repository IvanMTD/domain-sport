package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Data
@Table(name = "sport_object")
@NoArgsConstructor
public class SportObject {
    @Id
    private long id;

    private String title;
    protected String location;
    private String address;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate registerDate;
    private String url;
    private float s;
    private float d;
    private long logoId;
    private Set<Long> imageIds = new HashSet<>();

    public String getRegDate(){
        return DateTimeFormatter.ofPattern("dd.MM.yyyy").format(registerDate);
    }
}
