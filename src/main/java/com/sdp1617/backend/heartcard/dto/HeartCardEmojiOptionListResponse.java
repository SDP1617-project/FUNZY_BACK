package com.sdp1617.backend.heartcard.dto;

import com.sdp1617.backend.heartcard.entity.HeartCardEmojiType;

import java.util.Arrays;
import java.util.List;

public record HeartCardEmojiOptionListResponse(
        List<HeartCardEmojiOptionResponse> emojis
) {
    public static HeartCardEmojiOptionListResponse fromDefaultOptions() {
        return new HeartCardEmojiOptionListResponse(
                Arrays.stream(HeartCardEmojiType.values())
                        .map(HeartCardEmojiOptionResponse::from)
                        .toList()
        );
    }
}
