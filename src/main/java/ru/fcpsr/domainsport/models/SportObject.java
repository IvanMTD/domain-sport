package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class SportObject {
    @Id
    private long id;

    private String title;
    protected String location;
    private String address;
    private LocalDate registerDate;
    private String url;
    private float s;
    private float d;
    private long logoId;
    private Set<Long> imageIds = new HashSet<>();
}
