package ru.fcpsr.domainsport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeocoderResponseMetaData {
    @JsonProperty("request")
    private String request;

    @JsonProperty("results")
    private String results;

    @JsonProperty("found")
    private String found;
}
