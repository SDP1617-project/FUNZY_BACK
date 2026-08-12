package com.sdp1617.backend.auth.email;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        log.info("[EMAIL] to={}, subject={}, body={}", to, subject, body);
    }
}
