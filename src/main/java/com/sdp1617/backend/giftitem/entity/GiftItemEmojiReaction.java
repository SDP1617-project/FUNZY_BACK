package com.sdp1617.backend.giftitem.entity;

import com.sdp1617.backend.heartcard.entity.HeartCardEmojiType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "gift_item_emoji_reactions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_gift_item_emoji_item_member",
                columnNames = {"gift_item_id", "member_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GiftItemEmojiReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gift_item_id", nullable = false)
    private Long giftItemId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HeartCardEmojiType emoji;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public GiftItemEmojiReaction(Long giftItemId, Long memberId, HeartCardEmojiType emoji) {
        this.giftItemId = giftItemId;
        this.memberId = memberId;
        this.emoji = emoji;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void updateEmoji(HeartCardEmojiType emoji) {
        this.emoji = emoji;
        this.updatedAt = LocalDateTime.now();
    }
}
