package com.sdp1617.backend.archive.dto;

import com.sdp1617.backend.archive.entity.ArchiveCard;

public record ArchiveVisibilityResponse(
        boolean senderVisible,
        boolean receiverVisible,
        boolean dateVisible,
        boolean imageVisible,
        boolean messageVisible
) {
    public static ArchiveVisibilityResponse from(ArchiveCard card) {
        return new ArchiveVisibilityResponse(
                card.getVisibility().isSenderVisible(),
                card.getVisibility().isReceiverVisible(),
                card.getVisibility().isDateVisible(),
                card.getVisibility().isImageVisible(),
                card.getVisibility().isMessageVisible()
        );
    }
}
