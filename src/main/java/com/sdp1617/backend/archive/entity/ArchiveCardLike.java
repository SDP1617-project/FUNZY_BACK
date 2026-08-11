package com.sdp1617.backend.archive.entity;

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
        name = "archive_card_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_archive_card_like_card_member",
                columnNames = {"archive_card_id", "member_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArchiveCardLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "archive_card_id", nullable = false)
    private Long archiveCardId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ArchiveCardLike(Long archiveCardId, Long memberId) {
        this.archiveCardId = archiveCardId;
        this.memberId = memberId;
        this.createdAt = LocalDateTime.now();
    }
}
