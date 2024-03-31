package ru.fcpsr.domainsport.dto.geocode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeocoderMetaData {
    @JsonProperty("precision")
    private String precision;

    @JsonProperty("text")
    private String text;

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("Address")
    private Address address;

    @JsonProperty("AddressDetails")
    private AddressDetails addressDetails;
}
