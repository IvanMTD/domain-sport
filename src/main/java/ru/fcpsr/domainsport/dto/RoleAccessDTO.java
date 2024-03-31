package ru.fcpsr.domainsport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.fcpsr.domainsport.enums.Permission;
import ru.fcpsr.domainsport.models.RoleAccess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class RoleAccessDTO {
    private long id;

    private long userId;
    private long groupId; // напрямую связан со sport_id
    private Set<Permission> permissionList = new HashSet<>();
    private Set<Long> objectAccessIds = new HashSet<>();
    private List<ObjectAccessDTO> objectAccess = new ArrayList<>();

    public RoleAccessDTO(RoleAccess roleAccess) {
        setId(roleAccess.getId());
        setGroupId(roleAccess.getGroupId());
        setPermissionList(roleAccess.getPermissionList());
        setObjectAccessIds(roleAccess.getObjectAccessIds());
    }
}
