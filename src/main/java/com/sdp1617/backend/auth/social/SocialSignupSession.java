package com.sdp1617.backend.auth.social;

import com.sdp1617.backend.auth.entity.AuthProvider;

public record SocialSignupSession(
        AuthProvider provider,
        String externalId,
        String email
) {
}
