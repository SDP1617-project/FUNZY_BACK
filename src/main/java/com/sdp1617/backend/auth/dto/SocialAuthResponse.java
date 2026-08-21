package com.sdp1617.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SocialAuthResponse(
        @Schema(description = "신규 가입 여부. true면 signupToken으로 회원가입을 마저 완료해야 함", example = "false")
        boolean isNewUser,

        @Schema(description = "기존 회원인 경우에만 채워짐 (신규 회원이면 null)")
        TokenResponse tokens,

        @Schema(description = "신규 회원인 경우에만 채워짐. /signup/complete 호출 시 사용 (15분 유효)",
                example = "e7a4e358-2b9b-40f2-ad19-cf85641806f9")
        String signupToken,

        @Schema(description = "신규 회원인 경우에만 채워짐. 소셜 계정에서 받아온 이메일", example = "test@gmail.com")
        String email
) {
    public static SocialAuthResponse existingUser(TokenResponse tokens) {
        return new SocialAuthResponse(false, tokens, null, null);
    }

    public static SocialAuthResponse newUser(String signupToken, String email) {
        return new SocialAuthResponse(true, null, signupToken, email);
    }
}
