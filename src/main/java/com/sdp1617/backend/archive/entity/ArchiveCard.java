package com.sdp1617.backend.archive.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "archive_cards",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_archive_card_owner_letter",
                columnNames = {"owner_member_id", "letter_card_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArchiveCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_member_id", nullable = false)
    private Long ownerMemberId;

    @Column(name = "letter_card_id", nullable = false)
    private Long letterCardId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArchiveCategory category;

    @Column(length = 100)
    private String senderName;

    @Column(length = 100)
    private String receiverName;

    private LocalDate letterDate;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 2000)
    private String message;

    @Column(nullable = false)
    private int likeCount;

    @Embedded
    private ArchiveVisibility visibility = new ArchiveVisibility();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ArchiveCard(Long ownerMemberId, Long letterCardId, ArchiveCategory category) {
        this.ownerMemberId = ownerMemberId;
        this.letterCardId = letterCardId;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isOwnedBy(Long memberId) {
        return ownerMemberId.equals(memberId);
    }

    public void updateVisibility(ArchiveVisibility visibility) {
        this.visibility = visibility;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
