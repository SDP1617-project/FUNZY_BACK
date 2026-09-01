package com.sdp1617.backend.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Consent(
        @Column(name = "terms_agreed", nullable = false)
        boolean termsAgreed,

        @Column(name = "age14_confirmed", nullable = false)
        boolean age14Confirmed,

        @Column(name = "marketing_consent", nullable = false)
        boolean marketingConsent,

        @Column(name = "ad_consent", nullable = false)
        boolean adConsent
) {
    public static Consent requiredOnly() {
        return new Consent(true, true, false, false);
    }
}
