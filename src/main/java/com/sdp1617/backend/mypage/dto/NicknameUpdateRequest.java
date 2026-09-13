package com.sdp1617.backend.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameUpdateRequest(
        @Schema(description = "새 닉네임", example = "달콤한하루")
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자 이내여야 합니다.")
        String nickname
) {
    public NicknameUpdateRequest {
        nickname = nickname == null ? null : nickname.trim();
    }
}
