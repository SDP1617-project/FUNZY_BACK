package com.sdp1617.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AccountUnlockConfirmRequest(
        @Schema(description = "계정 잠금 해제 이메일로 발급된 토큰", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        @NotBlank(message = "토큰이 필요합니다.")
        String token
) {
}
