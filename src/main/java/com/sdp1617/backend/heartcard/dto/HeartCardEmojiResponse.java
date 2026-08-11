package com.sdp1617.backend.heartcard.dto;

import com.sdp1617.backend.heartcard.entity.HeartCardEmojiAction;
import com.sdp1617.backend.heartcard.entity.HeartCardEmojiReaction;
import com.sdp1617.backend.heartcard.entity.HeartCardEmojiType;

public record HeartCardEmojiResponse(
        Long heartCardId,
        HeartCardEmojiType emoji,
        boolean reacted,
        HeartCardEmojiAction action,
        boolean notificationCreated
) {
    public static HeartCardEmojiResponse empty(Long heartCardId) {
        return new HeartCardEmojiResponse(heartCardId, null, false, HeartCardEmojiAction.NONE, false);
    }

    public static HeartCardEmojiResponse from(HeartCardEmojiReaction reaction) {
        return new HeartCardEmojiResponse(reaction.getHeartCardId(), reaction.getEmoji(), true, HeartCardEmojiAction.NONE, false);
    }

    public static HeartCardEmojiResponse of(
            Long heartCardId,
            HeartCardEmojiType emoji,
            HeartCardEmojiAction action,
            boolean notificationCreated
    ) {
        return new HeartCardEmojiResponse(heartCardId, emoji, emoji != null, action, notificationCreated);
    }
}
