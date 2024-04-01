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
    private SportDTO sport;
    private List<Permission> permissionList = new ArrayList<>();
    private Set<Long> objectAccessIds = new HashSet<>();
    private List<ObjectAccessDTO> objectAccess = new ArrayList<>();

    public RoleAccessDTO(RoleAccess roleAccess) {
        setId(roleAccess.getId());
        setGroupId(roleAccess.getGroupId());
        setObjectAccessIds(roleAccess.getObjectAccessIds());
        permissionList.addAll(roleAccess.getPermissionList());
    }
}
