package com.sdp1617.backend.auth.email;

public interface EmailSender {

    void send(String to, String subject, String body);
}
