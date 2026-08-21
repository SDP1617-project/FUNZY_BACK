package com.sdp1617.backend.auth.dto;

import com.sdp1617.backend.auth.entity.AuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SocialLoginRequest(
        @Schema(description = "소셜 로그인 제공자", example = "KAKAO")
        @NotNull(message = "provider는 필수입니다.")
        AuthProvider provider,

        @Schema(description = "클라이언트가 발급받은 소셜 토큰 (카카오/네이버: access token, 구글: id_token)")
        @NotBlank(message = "token은 필수입니다.")
        String token
) {
}
