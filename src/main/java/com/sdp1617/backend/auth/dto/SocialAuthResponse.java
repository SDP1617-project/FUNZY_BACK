package com.sdp1617.backend.auth.dto;

public record SocialAuthResponse(
        boolean isNewUser,
        TokenResponse tokens,
        String signupToken,
        String email
) {
    public static SocialAuthResponse existingUser(TokenResponse tokens) {
        return new SocialAuthResponse(false, tokens, null, null);
    }

    public static SocialAuthResponse newUser(String signupToken, String email) {
        return new SocialAuthResponse(true, null, signupToken, email);
    }
}
