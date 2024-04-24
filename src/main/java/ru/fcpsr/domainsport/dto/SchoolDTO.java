package ru.fcpsr.domainsport.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import ru.fcpsr.domainsport.models.School;

@Data
@NoArgsConstructor
public class SchoolDTO {
    private long id;

    @NotBlank(message = "Не может быть пустым")
    private String name;
    private long ogrn;
    private int index;
    @NotBlank(message = "Не может быть пустым")
    private String address;
    private String url;
    private float s;
    private float d;

    @NotBlank(message = "Не может быть пустым")
    private String subject;
    private String description;

    private FilePart logo;
    private long logoId;
    private FilePart image;
    private long PhotoId;

    private boolean update;
    private boolean delete;

    public SchoolDTO(School school) {
        setId(school.getId());
        setName(school.getName());
        setOgrn(school.getOgrn());
        setIndex(school.getIndex());
        setAddress(school.getAddress());
        setUrl(school.getUrl());
        setS(school.getS());
        setD(school.getD());
        setSubject(school.getSubject());
        setDescription(school.getDescription());
        setLogoId(school.getLogoId());
        setPhotoId(school.getPhotoId());
    }
}
