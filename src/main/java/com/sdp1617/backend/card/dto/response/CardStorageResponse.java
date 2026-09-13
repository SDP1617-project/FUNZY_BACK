package com.sdp1617.backend.card.dto.response;

import com.sdp1617.backend.archive.entity.ArchiveCategory;
import com.sdp1617.backend.card.dto.DesignType;
import com.sdp1617.backend.card.entity.Card;

import java.time.LocalDateTime;

public record CardStorageResponse(
        Long cardId,
        Long envelopId,
        Long senderId,
        String senderNickname,
        Long receiverId,
        String receiverNickname,
        DesignType designType,
        String title,
        ArchiveCategory category,
        String link,
        String linkTitle,
        String content,
        String imageUrl,
        LocalDateTime createdAt
) {
    public static CardStorageResponse from(Card card) {
        return new CardStorageResponse(
                card.getId(),
                card.getEnvelop().getId(),
                card.getEnvelop().getSender().getId(),
                card.getEnvelop().getSender().getNickname(),
                card.getEnvelop().getReceiver().getId(),
                card.getEnvelop().getReceiver().getNickname(),
                card.getEnvelop().getDesignType(),
                card.getTitle(),
                card.getCategory(),
                card.getLink(),
                card.getLinkTitle(),
                card.getContent(),
                card.getImageUrl(),
                card.getCreatedAt()
        );
    }
}
