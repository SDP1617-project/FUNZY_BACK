package com.sdp1617.backend.auth.email;

public record VerificationLinkIssuedEvent(
        String to,
        String subject,
        String body
) {
}
