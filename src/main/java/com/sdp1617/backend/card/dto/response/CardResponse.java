package com.sdp1617.backend.card.dto.response;

import com.sdp1617.backend.card.entity.Card;

public record CardResponse(
        Long cardId,
        Long envelopId,
        String shareUrl
) {
    public static CardResponse from(Card card, String shareUrl) {
        return new CardResponse(card.getId(), card.getEnvelop().getId(), shareUrl);
    }
}
