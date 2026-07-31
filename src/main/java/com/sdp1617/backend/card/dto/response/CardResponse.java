package com.sdp1617.backend.card.dto.response;

import com.sdp1617.backend.card.entity.Card;

public record CardResponse(
        Long cardId,
        Long envelopId
) {
    public static CardResponse from(Card card) {
        return new CardResponse(card.getId(), card.getEnvelop().getId());
    }
}
