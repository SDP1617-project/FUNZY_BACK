package com.sdp1617.backend.auth.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender javaMailSender;

    @Override
    public void send(String to, String subject, String plainText, String htmlBody) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            // multipart=true여야 plainText를 text/plain 대안(alternative)으로 함께 보낼 수 있다.
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainText, htmlBody);
        } catch (MessagingException exception) {
            throw new MailParseException(exception);
        }
        javaMailSender.send(message);
    }
}
