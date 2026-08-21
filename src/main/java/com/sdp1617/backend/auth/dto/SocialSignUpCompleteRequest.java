package com.sdp1617.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SocialSignUpCompleteRequest(
        @Schema(description = "소셜 로그인 응답으로 받은 signupToken", example = "e7a4e358-2b9b-40f2-ad19-cf85641806f9")
        @NotBlank(message = "signupToken은 필수입니다.")
        String signupToken,

        @Schema(description = "닉네임 (2~20자, 중복 불가)", example = "닉네임")
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자 이내여야 합니다.")
        String nickname,

        @Schema(description = "서비스 이용약관 동의 여부 (true만 허용)", example = "true")
        @AssertTrue(message = "약관에 동의해야 가입할 수 있습니다.")
        boolean termsAgreed
) {
    public SocialSignUpCompleteRequest {
        nickname = nickname == null ? null : nickname.trim();
    }
}
