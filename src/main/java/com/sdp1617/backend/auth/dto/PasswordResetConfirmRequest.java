package com.sdp1617.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetConfirmRequest(
        @Schema(description = "비밀번호 재설정 이메일로 발급된 토큰", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        @NotBlank(message = "토큰이 필요합니다.")
        String token,

        @Schema(description = "새 비밀번호 (8자 이상, 영문+숫자+특수문자 조합)", example = "NewPassword1!")
        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Pattern(regexp = PasswordPolicy.REGEXP, message = PasswordPolicy.MESSAGE)
        @MaxUtf8Bytes(value = PasswordPolicy.MAX_BYTES, message = PasswordPolicy.MAX_BYTES_MESSAGE)
        String newPassword,

        @Schema(description = "새 비밀번호 확인", example = "NewPassword1!")
        @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
        String newPasswordConfirm
) {
}
