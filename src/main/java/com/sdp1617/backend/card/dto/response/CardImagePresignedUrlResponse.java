package com.sdp1617.backend.card.dto.response;

import java.time.Instant;

public record CardImagePresignedUrlResponse(
        String uploadUrl,
        String imageKey,
        String imageUrl,
        String method,
        Instant expiresAt
) {
}
