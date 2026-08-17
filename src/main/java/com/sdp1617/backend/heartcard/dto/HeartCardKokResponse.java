package com.sdp1617.backend.heartcard.dto;

import com.sdp1617.backend.archive.entity.ArchiveCard;
import com.sdp1617.backend.archive.entity.ArchiveCategory;

public record HeartCardKokResponse(
        Long heartCardId,
        boolean kok,
        Long archiveCardId,
        ArchiveCategory category
) {
    public static HeartCardKokResponse active(Long heartCardId, ArchiveCard archiveCard) {
        return new HeartCardKokResponse(heartCardId, true, archiveCard.getId(), archiveCard.getCategory());
    }

    public static HeartCardKokResponse inactive(Long heartCardId) {
        return new HeartCardKokResponse(heartCardId, false, null, null);
    }
}
