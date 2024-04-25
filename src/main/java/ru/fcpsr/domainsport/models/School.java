package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "school")
@NoArgsConstructor
public class School {
    @Id
    private long id;

    private String name;
    private long ogrn;
    private int index;
    private String address;
    private String url;
    private float s;
    private float d;

    private String subject;

    private long logoId;
    private long PhotoId;
}