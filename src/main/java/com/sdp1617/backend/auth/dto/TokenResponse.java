package com.sdp1617.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
        @Schema(description = "API 요청 시 Authorization 헤더에 사용하는 access token (1시간 유효)", example = "eyJhbGciOiJIUzM4NCJ9...")
        String accessToken,

        @Schema(description = "access token 재발급에 사용하는 refresh token (30일 유효)", example = "eyJhbGciOiJIUzM4NCJ9...")
        String refreshToken
) {
}
