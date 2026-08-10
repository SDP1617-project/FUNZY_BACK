package com.sdp1617.backend.archive.dto;

import com.sdp1617.backend.archive.entity.ArchiveCategory;

import java.util.List;

public record ArchiveCategorySectionResponse(
        ArchiveCategory category,
        String displayName,
        List<ArchiveCardResponse> cards,
        boolean empty
) {
    public static ArchiveCategorySectionResponse of(ArchiveCategory category, List<ArchiveCardResponse> cards) {
        return new ArchiveCategorySectionResponse(category, category.getDisplayName(), cards, cards.isEmpty());
    }
}
