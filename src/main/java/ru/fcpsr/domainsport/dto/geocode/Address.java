package ru.fcpsr.domainsport.dto.geocode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Address {
    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("formatted")
    private String formatted;

    @JsonProperty("Components")
    private List<Component> components;
}
