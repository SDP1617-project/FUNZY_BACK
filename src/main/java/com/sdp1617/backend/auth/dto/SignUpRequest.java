package com.sdp1617.backend.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Pattern(regexp = PasswordPolicy.REGEXP, message = PasswordPolicy.MESSAGE)
        String password,

        @NotBlank(message = "비밀번호 확인을 입력해주세요.")
        String passwordConfirm,

        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자 이내여야 합니다.")
        String nickname,

        @AssertTrue(message = "약관에 동의해야 가입할 수 있습니다.")
        boolean termsAgreed
) {
    public SignUpRequest {
        email = email == null ? null : email.trim();
        nickname = nickname == null ? null : nickname.trim();
    }
}
