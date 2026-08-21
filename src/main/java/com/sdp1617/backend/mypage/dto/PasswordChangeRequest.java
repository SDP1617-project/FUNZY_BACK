package com.sdp1617.backend.mypage.dto;

import com.sdp1617.backend.auth.dto.MaxUtf8Bytes;
import com.sdp1617.backend.auth.dto.PasswordPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordChangeRequest(
        @Schema(description = "현재 비밀번호", example = "OldPassword1!")
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String currentPassword,

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
