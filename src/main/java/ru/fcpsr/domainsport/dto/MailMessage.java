package ru.fcpsr.domainsport.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.fcpsr.domainsport.enums.Access;
import ru.fcpsr.domainsport.enums.Role;

@Data
public class MailMessage {
    @Email(message = "Не валидный email")
    @NotBlank(message = "Не может быть пустым")
    private String mail;
    private String title;
    private String message;

    private long sportId;
    private Role role;
    private Access access;
}
