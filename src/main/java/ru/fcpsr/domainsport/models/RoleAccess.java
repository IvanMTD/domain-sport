package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import ru.fcpsr.domainsport.enums.Permission;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class RoleAccess {
    @Id
    private long id;

    private long userId;
    private long groupId; // напрямую связан со sport_id
    private Set<Permission> permissionList = new HashSet<>();
    private Set<Long> objectAccessIds = new HashSet<>();

    public void setPermissions(List<Permission> permissions){
        for(Permission permission : permissions){
            permissionList.add(permission);
        }
    }
}
