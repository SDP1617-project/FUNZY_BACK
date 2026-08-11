package com.sdp1617.backend.heartcard.dto;

import com.sdp1617.backend.heartcard.entity.HeartCardEmojiType;

public record HeartCardEmojiOptionResponse(
        HeartCardEmojiType code,
        String label
) {
    public static HeartCardEmojiOptionResponse from(HeartCardEmojiType type) {
        return new HeartCardEmojiOptionResponse(type, type.getLabel());
    }
}
