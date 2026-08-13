package com.sdp1617.backend.social.dto;

import jakarta.validation.constraints.NotBlank;

public record FollowRequestCreateRequest(
        @NotBlank(message = "친구 코드를 입력해주세요.")
        String followCode
) {
}
