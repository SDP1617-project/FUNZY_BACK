package com.sdp1617.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AccessTokenResponse(
        @Schema(description = "재발급된 access token", example = "eyJhbGciOiJIUzM4NCJ9...")
        String accessToken
) {
}
