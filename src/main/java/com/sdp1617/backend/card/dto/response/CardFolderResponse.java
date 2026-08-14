package com.sdp1617.backend.card.dto.response;

import java.time.LocalDateTime;

public record CardFolderResponse(
        Long memberId,
        String nickname,
        long cardCount,
        String latestImageUrl,
        LocalDateTime latestCardCreatedAt
) {
}
