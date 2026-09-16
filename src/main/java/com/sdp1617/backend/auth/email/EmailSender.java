package com.sdp1617.backend.auth.email;

public interface EmailSender {

    /** @param plainText HTML을 못 읽는 클라이언트/스팸 필터 대비용 대체 본문 */
    void send(String to, String subject, String plainText, String htmlBody);
}
