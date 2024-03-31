package ru.fcpsr.domainsport.dto;

import lombok.Data;
import ru.fcpsr.domainsport.enums.Role;

@Data
public class MailMessage {
    private String mail;
    private String title;
    private String message;

    private long sportId;
    private Role role;
}
