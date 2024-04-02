package ru.fcpsr.domainsport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PasswordDTO {
    @NotBlank(message = "Введите старый пароль")
    private String oldPassword;
    @NotBlank(message = "Нельзя указать пустой пароль")
    @Size(min = 8, max = 14, message = "Длинна пароля ограничена от 8 до 14 символов")
    private String password;
    @NotBlank(message = "Нельзя указать пустое подтверждение пароля")
    private String confirmPassword;
}
