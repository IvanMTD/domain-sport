package ru.fcpsr.domainsport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.fcpsr.domainsport.models.ObjectAccess;

@Data
@NoArgsConstructor
public class ObjectAccessDTO {
    private long id;
    private long roleAccessId;
    private String className;
    private long objectId;

    public ObjectAccessDTO(ObjectAccess objectAccess) {
        setId(objectAccess.getId());
        setRoleAccessId(objectAccess.getRoleAccessId());
        setClassName(objectAccess.getClassName());
        setObjectId(objectAccess.getObjectId());
    }
}
