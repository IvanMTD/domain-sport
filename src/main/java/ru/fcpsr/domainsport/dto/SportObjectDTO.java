package ru.fcpsr.domainsport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.fcpsr.domainsport.models.SportObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class SportObjectDTO {
    private long id;

    private String title;
    protected String location;
    private String address;
    private LocalDate registerDate;
    private String url;
    private float s;
    private float d;
    private long logoId;
    private List<Long> imageIds = new ArrayList<>();

    private String present;

    public SportObjectDTO(SportObject sportObject){
        setId(sportObject.getId());
        setTitle(sportObject.getTitle());
        setLocation(sportObject.getLocation());
        setAddress(sportObject.getAddress());
        setRegisterDate(sportObject.getRegisterDate());
        setUrl(sportObject.getUrl());
        setS(sportObject.getS());
        setD(sportObject.getD());
        setLogoId(sportObject.getLogoId());
        imageIds.addAll(sportObject.getImageIds());
        setPresent("islands#blueSportIcon");
    }
}
