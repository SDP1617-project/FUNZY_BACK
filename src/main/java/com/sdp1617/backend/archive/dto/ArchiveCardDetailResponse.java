package com.sdp1617.backend.archive.dto;

import com.sdp1617.backend.archive.entity.ArchiveCard;
import com.sdp1617.backend.archive.entity.ArchiveCategory;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ArchiveCardDetailResponse(
        Long archiveCardId,
        Long letterCardId,
        ArchiveCategory category,
        String senderName,
        String receiverName,
        LocalDate letterDate,
        String imageUrl,
        String message,
        int likeCount,
        boolean liked,
        ArchiveVisibilityResponse visibility,
        LocalDateTime archivedAt
) {
    public static ArchiveCardDetailResponse from(ArchiveCard card, boolean friendView, boolean liked) {
        boolean showSender = !friendView || card.getVisibility().isSenderVisible();
        boolean showReceiver = !friendView || card.getVisibility().isReceiverVisible();
        boolean showDate = !friendView || card.getVisibility().isDateVisible();
        boolean showImage = !friendView || card.getVisibility().isImageVisible();
        boolean showMessage = !friendView || card.getVisibility().isMessageVisible();

        return new ArchiveCardDetailResponse(
                card.getId(),
                card.getLetterCardId(),
                card.getCategory(),
                showSender ? card.getSenderName() : null,
                showReceiver ? card.getReceiverName() : null,
                showDate ? card.getLetterDate() : null,
                showImage ? card.getImageUrl() : null,
                showMessage ? card.getMessage() : null,
                card.getLikeCount(),
                liked,
                ArchiveVisibilityResponse.from(card),
                card.getCreatedAt()
        );
    }
}
