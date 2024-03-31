package ru.fcpsr.domainsport.dto.geocode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Response {
    @JsonProperty("GeoObjectCollection")
    private GeoObjectCollection geoObjectCollection;
}
