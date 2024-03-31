package ru.fcpsr.domainsport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.fcpsr.domainsport.enums.Role;
import ru.fcpsr.domainsport.models.AppUser;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class AppUserDTO {
    private long id;
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String email;
    private LocalDate birthday;
    private LocalDate placedAt;
    private String avatarId;
    private String oauthId;
    private Role role;

    private long roleAccessId;
    private RoleAccessDTO roleAccess;

    public AppUserDTO(AppUser user) {
        setId(user.getId());
        setUsername(user.getUsername());
        setFirstname(user.getFirstname());
        setLastname(user.getLastname());
        setEmail(user.getEmail());
        setBirthday(user.getBirthday());
        setPlacedAt(user.getPlacedAt());
        setAvatarId(user.getAvatarId());
        setRole(user.getRole());
    }

    public String getFullName(){
        return lastname + " " + firstname;
    }
}
