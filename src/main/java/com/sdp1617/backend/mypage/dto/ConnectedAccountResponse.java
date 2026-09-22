package com.sdp1617.backend.mypage.dto;

import com.sdp1617.backend.auth.entity.AuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ConnectedAccountResponse(
        @Schema(description = "연결된 소셜 provider 목록 (회원당 여러 개 연결 가능)", example = "[\"KAKAO\", \"GOOGLE\"]")
        List<AuthProvider> connectedProviders,

        @Schema(description = "비밀번호 보유 여부. false면 비밀번호 변경 메뉴를 노출하지 않아야 함", example = "false")
        boolean hasPassword
) {
}
