package ru.fcpsr.domainsport.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;

@Data
@NoArgsConstructor
public class AppUser implements UserDetails {
    /*
        Как получить аватар!
        https://avatars.yandex.net/get-yapic/<идентификатор портрета>/<размер>
        islands-small — 28×28 пикселей.
        islands-34 — 34×34 пикселей.
        islands-middle — 42×42 пикселей.
        islands-50 — 50×50 пикселей.
        islands-retina-small — 56×56 пикселей.
        islands-68 — 68×68 пикселей.
        islands-75 — 75×75 пикселей.
        islands-retina-middle — 84×84 пикселей.
        islands-retina-50 — 100×100 пикселей.
        islands-200 — 200×200 пикселей.
     */
    @Id
    private long id;
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String email;
    private LocalDate birthday;
    private LocalDate placedAt;
    private String avatarId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new HashSet<>();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
