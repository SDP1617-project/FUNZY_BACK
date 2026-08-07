package com.sdp1617.backend.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
