package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "object_access")
@NoArgsConstructor
public class ObjectAccess {
    @Id
    private long id;
    private long roleAccessId;
    private String className;
    private long objectId;
}
