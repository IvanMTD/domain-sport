package ru.fcpsr.domainsport.dto.geocode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddressDetails {
    @JsonProperty("Country")
    private Country country;
}
