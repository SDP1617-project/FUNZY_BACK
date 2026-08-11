package com.sdp1617.backend.archive.dto;

public record ArchiveLikeResponse(
        Long archiveCardId,
        int likeCount,
        boolean liked
) {
}
