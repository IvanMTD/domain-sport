package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class Ekp {
    @Id
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
}
