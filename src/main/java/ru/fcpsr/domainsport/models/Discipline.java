package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "discipline")
@NoArgsConstructor
public class Discipline {
    @Id
    private long id;
    private String title;
    private String description;
    private long sportId;
}
