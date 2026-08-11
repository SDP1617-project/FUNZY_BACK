package com.sdp1617.backend.heartcard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "heart_card_phrase_comments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_heart_card_phrase_comment_range",
                columnNames = {"heart_card_id", "start_offset", "end_offset"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HeartCardPhraseComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "heart_card_id", nullable = false)
    private Long heartCardId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "start_offset", nullable = false)
    private int startOffset;

    @Column(name = "end_offset", nullable = false)
    private int endOffset;

    @Column(nullable = false, length = 500)
    private String selectedText;

    @Column(nullable = false, length = 50)
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public HeartCardPhraseComment(
            Long heartCardId,
            Long memberId,
            int startOffset,
            int endOffset,
            String selectedText,
            String content
    ) {
        this.heartCardId = heartCardId;
        this.memberId = memberId;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.selectedText = selectedText;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public boolean isWrittenBy(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
}
