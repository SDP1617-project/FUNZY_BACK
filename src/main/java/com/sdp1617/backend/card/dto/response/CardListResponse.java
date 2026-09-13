package com.sdp1617.backend.card.dto.response;

import com.sdp1617.backend.archive.entity.ArchiveCategory;
import com.sdp1617.backend.card.dto.DesignType;
import com.sdp1617.backend.card.entity.Card;

public record CardListResponse(
        Long cardId,
        Long envelopId,
        Long receiverId,
        String receiverNickname,
        DesignType designType,
        String title,
        ArchiveCategory category,
        String link,
        String linkTitle,
        String content,
        String imageUrl
) {
    public static CardListResponse from(Card card) {
        return new CardListResponse(
                card.getId(),
                card.getEnvelop().getId(),
                card.getEnvelop().getReceiver().getId(),
                card.getEnvelop().getReceiver().getNickname(),
                card.getEnvelop().getDesignType(),
                card.getTitle(),
                card.getCategory(),
                card.getLink(),
                card.getLinkTitle(),
                card.getContent(),
                card.getImageUrl()
        );
    }
}
