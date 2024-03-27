package ru.fcpsr.domainsport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeoObjectMetaData {
    @JsonProperty("GeocoderMetaData")
    private GeocoderMetaData geocoderMetaData;
}
