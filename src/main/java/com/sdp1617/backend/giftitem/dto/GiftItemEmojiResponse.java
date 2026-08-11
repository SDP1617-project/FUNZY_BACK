package com.sdp1617.backend.giftitem.dto;

import com.sdp1617.backend.giftitem.entity.GiftItemEmojiReaction;
import com.sdp1617.backend.heartcard.entity.HeartCardEmojiAction;
import com.sdp1617.backend.heartcard.entity.HeartCardEmojiType;

public record GiftItemEmojiResponse(
        Long giftItemId,
        HeartCardEmojiType emoji,
        boolean reacted,
        HeartCardEmojiAction action,
        boolean notificationCreated
) {
    public static GiftItemEmojiResponse empty(Long giftItemId) {
        return new GiftItemEmojiResponse(giftItemId, null, false, HeartCardEmojiAction.NONE, false);
    }

    public static GiftItemEmojiResponse from(GiftItemEmojiReaction reaction) {
        return new GiftItemEmojiResponse(reaction.getGiftItemId(), reaction.getEmoji(), true, HeartCardEmojiAction.NONE, false);
    }

    public static GiftItemEmojiResponse of(
            Long giftItemId,
            HeartCardEmojiType emoji,
            HeartCardEmojiAction action,
            boolean notificationCreated
    ) {
        return new GiftItemEmojiResponse(giftItemId, emoji, emoji != null, action, notificationCreated);
    }
}
