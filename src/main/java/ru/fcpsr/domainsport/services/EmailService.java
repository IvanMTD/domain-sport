package ru.fcpsr.domainsport.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.fcpsr.domainsport.dto.MailMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final String fromMail = "morgan-t-darck@yandex.ru";
    private final JavaMailSender mailSender;

    public void sendEmail(MailMessage mailMessage) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false,"utf-8");

        helper.setFrom(fromMail);
        helper.setTo(mailMessage.getMail());
        helper.setText(mailMessage.getMessage(),true);
        helper.setSubject(mailMessage.getTitle());

        mailSender.send(message);
        log.info("mail {} sent", mailMessage);
    }
}
