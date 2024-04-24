package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import ru.fcpsr.domainsport.enums.Access;
import ru.fcpsr.domainsport.enums.Role;

@Data
@Table(name = "auth_token")
@NoArgsConstructor
public class AuthToken {
    @Id
    private long id;
    private String link;
    private String email;
    private boolean status;

    private Role role;
    private Access access;
    private long sportId;
}
