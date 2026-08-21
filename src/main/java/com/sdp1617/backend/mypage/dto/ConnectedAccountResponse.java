package com.sdp1617.backend.mypage.dto;

import com.sdp1617.backend.auth.entity.AuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;

public record ConnectedAccountResponse(
        @Schema(description = "가입 방식", example = "KAKAO")
        AuthProvider provider,

        @Schema(description = "비밀번호 보유 여부. false면 소셜 전용 계정이라 비밀번호 변경 메뉴를 노출하지 않아야 함", example = "false")
        boolean hasPassword
) {
}
