package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import ru.fcpsr.domainsport.enums.Role;

@Data
@NoArgsConstructor
public class AuthToken {
    @Id
    private long id;
    private String link;
    private String email;
    private boolean status;

    private Role role;
    private long sportId;
}
