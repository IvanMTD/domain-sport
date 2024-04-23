package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import ru.fcpsr.domainsport.enums.Access;
import ru.fcpsr.domainsport.enums.Permission;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Table(name = "role_access")
@NoArgsConstructor
public class RoleAccess {
    @Id
    private long id;

    private long userId;
    private long groupId; // напрямую связан со sport_id

    private Access access;
    private Set<Permission> permissionList = new HashSet<>();
    private Set<Long> objectAccessIds = new HashSet<>();

    public void setPermissions(List<Permission> permissions){
        permissionList.addAll(permissions);
    }
}
