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
    private final String fromMail = "domensport@yandex.ru";
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

    public MailMessage getMailMessageForPassword(String email, String password){
        MailMessage mailMessage = new MailMessage();
        mailMessage.setMail(email);
        mailMessage.setTitle("Вы успешно прошли регистрацию");
        mailMessage.setMessage(getHtmlStringForPassword(password));
        return mailMessage;
    }

    private String getHtmlStringForPassword(String password){
        String message = "" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                "    <title>Приглашение</title>\n" +
                "    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH\" crossorigin=\"anonymous\">" +
                "</head>\n" +
                "<body>\n" +
                "   <div class=\"container\">\n" +
                "     <div class=\"row\">\n" +
                "       <div class=\"col d-flex align-items-center\" style=\"height: 500px\">\n" +
                "         <div class=\"card mx-auto\" style=\"width: 18rem;\">\n" +
                "           <img src=\"https://domensport.ru/img/logo.png\" class=\"card-img-top p-2\" alt=\"...\">\n" +
                "           <div class=\"card-body\">\n" +
                "             <h5 class=\"card-title\">Вы успешно зарегистрировались</h5>\n" +
                "             <p class=\"card-text\">Ваш пароль: <b>" + password + "</b></p>\n" +
                "           </div>\n" +
                "         </div>\n" +
                "       </div>\n" +
                "     </div>\n" +
                "   </div>" +
                "   <script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js\" integrity=\"sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz\" crossorigin=\"anonymous\"></script>" +
                "</body>\n" +
                "</html>";
        return message;
    }
}
