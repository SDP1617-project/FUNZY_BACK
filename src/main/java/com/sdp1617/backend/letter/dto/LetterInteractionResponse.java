package com.sdp1617.backend.letter.dto;

import com.sdp1617.backend.letter.entity.LetterInteraction;
import com.sdp1617.backend.letter.entity.LetterInteractionType;

import java.time.LocalDateTime;

public record LetterInteractionResponse(
        Long interactionId,
        Long letterId,
        Long memberId,
        LetterInteractionType type,
        String value,
        LocalDateTime createdAt
) {
    public static LetterInteractionResponse from(LetterInteraction interaction) {
        return new LetterInteractionResponse(
                interaction.getId(),
                interaction.getLetterId(),
                interaction.getMemberId(),
                interaction.getType(),
                interaction.getValue(),
                interaction.getCreatedAt()
        );
    }
}
