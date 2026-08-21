package com.sdp1617.backend.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TermsResponse(
        @Schema(description = "이용약관 문서 URL", example = "https://sdp1617.example.com/terms")
        String termsOfServiceUrl,

        @Schema(description = "개인정보처리방침 문서 URL", example = "https://sdp1617.example.com/privacy")
        String privacyPolicyUrl
) {
}
