package com.sdp1617.backend.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @Schema(description = "로그아웃할 세션의 refresh token (해당 기기 세션만 종료됨)", example = "eyJhbGciOiJIUzM4NCJ9...")
        @NotBlank(message = "refreshToken은 필수입니다.")
        String refreshToken
) {
}
