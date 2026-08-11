package com.sdp1617.backend.funzypack.dto;

import com.sdp1617.backend.funzypack.entity.FunzyPackCard;

public record FunzyPackCardResponse(
        Long heartCardId,
        int cardOrder,
        String imageUrl,
        String message
) {
    public static FunzyPackCardResponse from(FunzyPackCard card) {
        return new FunzyPackCardResponse(
                card.getHeartCardId(),
                card.getCardOrder(),
                card.getImageUrl(),
                card.getMessage()
        );
    }
}
