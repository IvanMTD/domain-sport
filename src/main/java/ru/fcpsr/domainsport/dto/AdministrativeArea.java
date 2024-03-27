package ru.fcpsr.domainsport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdministrativeArea {
    @JsonProperty("AdministrativeAreaName")
    private String administrativeAreaName;

    @JsonProperty("SubAdministrativeArea")
    private SubAdministrativeArea subAdministrativeArea;
}
