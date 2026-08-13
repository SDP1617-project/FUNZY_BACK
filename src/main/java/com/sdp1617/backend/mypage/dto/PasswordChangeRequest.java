package com.sdp1617.backend.mypage.dto;

import com.sdp1617.backend.auth.dto.MaxUtf8Bytes;
import com.sdp1617.backend.auth.dto.PasswordPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordChangeRequest(
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Pattern(regexp = PasswordPolicy.REGEXP, message = PasswordPolicy.MESSAGE)
        @MaxUtf8Bytes(value = PasswordPolicy.MAX_BYTES, message = PasswordPolicy.MAX_BYTES_MESSAGE)
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
        String newPasswordConfirm
) {
}
