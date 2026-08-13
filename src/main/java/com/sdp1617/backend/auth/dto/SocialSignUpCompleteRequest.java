package com.sdp1617.backend.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SocialSignUpCompleteRequest(
        @NotBlank(message = "signupToken은 필수입니다.")
        String signupToken,

        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자 이내여야 합니다.")
        String nickname,

        @AssertTrue(message = "약관에 동의해야 가입할 수 있습니다.")
        boolean termsAgreed
) {
    public SocialSignUpCompleteRequest {
        nickname = nickname == null ? null : nickname.trim();
    }
}
