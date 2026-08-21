package com.sdp1617.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequest(
        @Schema(description = "로그인 시 발급받은 refresh token", example = "eyJhbGciOiJIUzM4NCJ9...")
        @NotBlank(message = "refreshToken은 필수입니다.")
        String refreshToken
) {
}
