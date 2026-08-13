package com.sdp1617.backend.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SessionRevocationListener {

    private final TokenService tokenService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AllSessionsRevokedEvent event) {
        tokenService.revokeAllSessions(event.memberId());
    }
}
