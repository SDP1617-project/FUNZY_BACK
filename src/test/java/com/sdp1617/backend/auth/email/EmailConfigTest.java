package com.sdp1617.backend.auth.email;

import org.junit.jupiter.api.Test;
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class EmailConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(MailSenderAutoConfiguration.class))
            .withUserConfiguration(EmailConfig.class);

    @Test
    void local_프로필에서는_LoggingEmailSender만_등록된다() {
        contextRunner
                .withPropertyValues("app.mail.enabled=false")
                .withPropertyValues("spring.profiles.active=local")
                .withInitializer(ctx -> ctx.getEnvironment().addActiveProfile("local"))
                .run(context -> {
                    assertThat(context).hasSingleBean(EmailSender.class);
                    assertThat(context.getBean(EmailSender.class)).isInstanceOf(LoggingEmailSender.class);
                });
    }

    @Test
    void mail_enabled가_true면_SmtpEmailSender만_등록된다() {
        contextRunner
                .withPropertyValues(
                        "app.mail.enabled=true",
                        "spring.mail.host=smtp.example.com"
                )
                .withInitializer(ctx -> ctx.getEnvironment().addActiveProfile("local"))
                .run(context -> {
                    assertThat(context).hasSingleBean(EmailSender.class);
                    assertThat(context.getBean(EmailSender.class)).isInstanceOf(SmtpEmailSender.class);
                });
    }

    @Test
    void ci_프로필에서도_LoggingEmailSender가_등록된다() {
        contextRunner
                .withPropertyValues("app.mail.enabled=false")
                .withInitializer(ctx -> ctx.getEnvironment().addActiveProfile("prod"))
                .withInitializer(ctx -> ctx.getEnvironment().addActiveProfile("ci"))
                .run(context -> {
                    assertThat(context).hasSingleBean(EmailSender.class);
                    assertThat(context.getBean(EmailSender.class)).isInstanceOf(LoggingEmailSender.class);
                });
    }

    @Test
    void local도_ci도_아니고_mail도_비활성이면_EmailSender빈이_없다() {
        contextRunner
                .withPropertyValues("app.mail.enabled=false")
                .withInitializer(ctx -> ctx.getEnvironment().addActiveProfile("prod"))
                .run(context -> assertThat(context).doesNotHaveBean(EmailSender.class));
    }
}
