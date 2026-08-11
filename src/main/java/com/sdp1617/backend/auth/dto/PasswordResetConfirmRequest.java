package com.sdp1617.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetConfirmRequest(
        @NotBlank(message = "토큰이 필요합니다.")
        String token,

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Pattern(regexp = PasswordPolicy.REGEXP, message = PasswordPolicy.MESSAGE)
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
        String newPasswordConfirm
) {
}
