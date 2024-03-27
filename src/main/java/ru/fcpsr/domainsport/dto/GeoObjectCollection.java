package ru.fcpsr.domainsport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GeoObjectCollection {
    @JsonProperty("metaDataProperty")
    private MetaDataProperty metaDataProperty;

    @JsonProperty("featureMember")
    private List<FeatureMember> featureMember;
}
