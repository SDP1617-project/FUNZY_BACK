package com.sdp1617.backend.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FollowCodeResponse(
        @Schema(description = "내 팔로우 코드 (8자리 영숫자, 타인에게 공유해서 팔로우 요청받는 용도)", example = "S4UEFSAD")
        String followCode
) {
}
