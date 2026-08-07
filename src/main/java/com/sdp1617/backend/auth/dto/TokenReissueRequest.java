package com.sdp1617.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequest(
        @NotBlank(message = "refreshToken은 필수입니다.")
        String refreshToken
) {
}
