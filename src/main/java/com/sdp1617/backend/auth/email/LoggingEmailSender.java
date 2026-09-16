package com.sdp1617.backend.auth.email;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String plainText, String htmlBody) {
        // 평소엔 plainText만 INFO로 남기고, 템플릿 확인이 필요할 때만 DEBUG로 HTML 전체를 본다.
        log.info("[EMAIL] to={}, subject={}, body={}", to, subject, plainText);
        log.debug("[EMAIL][html] to={}, subject={}, html={}", to, subject, htmlBody);
    }
}
