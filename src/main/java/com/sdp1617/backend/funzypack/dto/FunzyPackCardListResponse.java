package com.sdp1617.backend.funzypack.dto;

import com.sdp1617.backend.funzypack.entity.FunzyPackCard;

import java.util.List;

public record FunzyPackCardListResponse(
        Long packId,
        boolean empty,
        int count,
        List<FunzyPackCardResponse> cards
) {
    public static FunzyPackCardListResponse of(Long packId, List<FunzyPackCard> cards) {
        List<FunzyPackCardResponse> responses = cards.stream()
                .map(FunzyPackCardResponse::from)
                .toList();
        return new FunzyPackCardListResponse(packId, responses.isEmpty(), responses.size(), responses);
    }
}
