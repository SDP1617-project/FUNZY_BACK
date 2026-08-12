package com.sdp1617.backend.auth.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class EmailConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "true")
    public EmailSender smtpEmailSender(JavaMailSender javaMailSender) {
        return new SmtpEmailSender(javaMailSender);
    }

    // 인증 링크(토큰)를 그대로 로그에 남기므로 local/ci 프로필에서만 등록한다.
    @Bean
    @ConditionalOnMissingBean(EmailSender.class)
    @Profile({"local", "ci"})
    public EmailSender loggingEmailSender() {
        return new LoggingEmailSender();
    }
}
