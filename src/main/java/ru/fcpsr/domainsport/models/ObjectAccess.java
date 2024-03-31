package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
public class ObjectAccess {
    @Id
    private long id;
    private long roleAccessId;
    private String className;
    private long objectId;
}
