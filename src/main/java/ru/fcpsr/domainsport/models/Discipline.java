package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
public class Discipline {
    @Id
    private int id;
    private String title;
    private String description;
    private long sportId;
}
