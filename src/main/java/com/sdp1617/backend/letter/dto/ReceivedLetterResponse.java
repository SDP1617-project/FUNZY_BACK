package com.sdp1617.backend.letter.dto;

import com.sdp1617.backend.letter.entity.ReceivedLetter;

import java.time.LocalDateTime;

public record ReceivedLetterResponse(
        Long letterId,
        String senderName,
        String receiverName,
        LocalDateTime receivedAt,
        int heartCardCount
) {
    public static ReceivedLetterResponse from(ReceivedLetter letter) {
        return new ReceivedLetterResponse(
                letter.getId(),
                letter.getSenderName(),
                letter.getReceiverName(),
                letter.getReceivedAt(),
                letter.getHeartCardCount()
        );
    }
}
