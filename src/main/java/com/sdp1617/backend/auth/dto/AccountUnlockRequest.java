package com.sdp1617.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AccountUnlockRequest(
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email
) {
    public AccountUnlockRequest {
        email = email == null ? null : email.trim();
    }
}
