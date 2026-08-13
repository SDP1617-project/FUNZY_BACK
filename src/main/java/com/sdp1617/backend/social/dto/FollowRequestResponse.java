package com.sdp1617.backend.social.dto;

import java.time.LocalDateTime;

public record FollowRequestResponse(
        Long requestId,
        Long requesterId,
        String requesterNickname,
        LocalDateTime createdAt
) {
}
