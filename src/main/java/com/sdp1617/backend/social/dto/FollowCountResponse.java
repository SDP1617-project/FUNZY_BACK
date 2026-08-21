package com.sdp1617.backend.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FollowCountResponse(
        @Schema(description = "현재 친구(맞팔) 수", example = "1")
        long currentCount,

        @Schema(description = "친구 수 상한 (기획 확정 전 임시값)", example = "50")
        int maxCount
) {
}
