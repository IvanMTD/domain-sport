package ru.fcpsr.domainsport.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.fcpsr.domainsport.enums.Role;
import ru.fcpsr.domainsport.models.AppUser;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class AppUserDTO {
    private long id;
    @NotBlank(message = "Псевдоним пользователя не может быть пустым")
    @Size(min = 4, max = 12, message = "Псевдоним пользователя должен быть от 4 до 12 символов")
    private String username;
    private String password;
    /*@NotBlank(message = "Введите старый пароль")
    private String oldPassword;
    @NotBlank(message = "Нельзя указать пустой пароль")
    @Size(min = 8, max = 14, message = "Длинна пароля ограничена от 8 до 14 символов")
    private String password;
    @NotBlank(message = "Нельзя указать пустое подтверждение пароля")
    private String confirmPassword;*/
    @NotBlank(message = "Поле имени не может быть пустым")
    @Pattern(regexp = "^[А-ЯA-Z][а-яa-z]+", message = "Поле заполняется в формате - \"Иван\"")
    private String firstname;
    @NotBlank(message = "Поле фамилия не может быть пустым")
    @Pattern(regexp = "^[А-ЯA-Z][а-яa-z]+", message = "Поле заполняется в формате - \"Иванов\"")
    private String lastname;
    @NotBlank(message = "Подтвердите свой адрес электронной почты")
    @Email(message = "Не валидный адрес электронной почты")
    private String email;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Укажите дату рождения")
    @Past(message = "Не валидная дата рождения")
    private LocalDate birthday;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate placedAt;
    private String avatarId;
    private String oauthId;
    private Role role;

    private Set<Long> roleAccessIds = new HashSet<>();
    private List<RoleAccessDTO> roleAccessList;

    public AppUserDTO(AppUser user) {
        setId(user.getId());
        setRoleAccessIds(user.getRoleAccessIds());
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
        return firstname + " " + lastname;
    }
}
