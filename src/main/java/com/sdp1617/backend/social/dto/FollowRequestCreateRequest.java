package com.sdp1617.backend.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record FollowRequestCreateRequest(
        @Schema(description = "상대방의 팔로우 코드", example = "S4UEFSAD")
        @NotBlank(message = "친구 코드를 입력해주세요.")
        String followCode
) {
}
