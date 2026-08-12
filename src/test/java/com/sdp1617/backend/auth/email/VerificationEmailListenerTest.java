package com.sdp1617.backend.auth.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VerificationEmailListenerTest {

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private VerificationEmailListener listener;

    @Test
    void 커밋_이후_이벤트를_받으면_이메일을_발송한다() {
        VerificationLinkIssuedEvent event = new VerificationLinkIssuedEvent("test@sdp1617.com", "제목", "본문");

        listener.handle(event);

        verify(emailSender).send("test@sdp1617.com", "제목", "본문");
    }
}
