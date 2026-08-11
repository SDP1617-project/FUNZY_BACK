package com.sdp1617.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AccountUnlockConfirmRequest(
        @NotBlank(message = "토큰이 필요합니다.")
        String token
) {
}
