package com.sdp1617.backend.auth.email;

import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmtpEmailSenderTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Test
    void 텍스트_HTML_두_본문을_함께_발송한다() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        SmtpEmailSender sender = new SmtpEmailSender(javaMailSender);
        sender.send("test@sdp1617.com", "제목", "텍스트 본문", "<p>HTML 본문</p>");
        mimeMessage.saveChanges(); // 실제 발송 시엔 JavaMailSenderImpl이 호출하지만, 여기선 send가 목이라 직접 호출해 헤더를 확정한다

        assertEquals("test@sdp1617.com", mimeMessage.getAllRecipients()[0].toString());
        assertEquals("제목", mimeMessage.getSubject());
        assertTrue(mimeMessage.getContentType().contains("multipart"));

        List<Part> leaves = new ArrayList<>();
        collectLeafParts(mimeMessage, leaves);

        boolean hasPlainText = leaves.stream().anyMatch(part -> isType(part, "text/plain") && contains(part, "텍스트 본문"));
        boolean hasHtml = leaves.stream().anyMatch(part -> isType(part, "text/html") && contains(part, "HTML 본문"));
        assertTrue(hasPlainText, "text/plain 대안이 없음");
        assertTrue(hasHtml, "text/html 본문이 없음");

        verify(javaMailSender).send(mimeMessage);
    }

    private void collectLeafParts(Part part, List<Part> leaves) throws Exception {
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                collectLeafParts(multipart.getBodyPart(i), leaves);
            }
        } else {
            leaves.add(part);
        }
    }

    private boolean isType(Part part, String mimeType) {
        try {
            return part.isMimeType(mimeType);
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean contains(Part part, String text) {
        try {
            return String.valueOf(part.getContent()).contains(text);
        } catch (Exception exception) {
            return false;
        }
    }
}
