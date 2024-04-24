package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import ru.fcpsr.domainsport.dto.SchoolDTO;

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
    private String description;

    private long logoId;
    private long PhotoId;

    public School(SchoolDTO schoolDTO) {
        setId(schoolDTO.getId());
        setName(schoolDTO.getName());
        setOgrn(schoolDTO.getOgrn());
        setIndex(schoolDTO.getIndex());
        setAddress(schoolDTO.getAddress());
        setUrl(schoolDTO.getUrl());

        setSubject(schoolDTO.getSubject());
        setDescription(schoolDTO.getDescription());
    }
}
