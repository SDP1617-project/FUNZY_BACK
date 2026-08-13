package com.sdp1617.backend.social.dto;

public record FollowCountResponse(
        long currentCount,
        int maxCount
) {
}
