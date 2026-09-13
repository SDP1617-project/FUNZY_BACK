package com.sdp1617.backend.mypage.dto;

import java.time.Instant;

public record ProfileImagePresignedUrlResponse(
        String uploadUrl,
        String imageKey,
        String imageUrl,
        String method,
        Instant expiresAt
) {
}
