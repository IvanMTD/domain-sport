package ru.fcpsr.domainsport.dto.geocode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeoObject {
    @JsonProperty("metaDataProperty")
    private GeoObjectMetaData geoObjectMetaData;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("boundedBy")
    private BoundedBy boundedBy;

    @JsonProperty("Point")
    private Point point;

    @JsonProperty("uri")
    private String uri;
}
