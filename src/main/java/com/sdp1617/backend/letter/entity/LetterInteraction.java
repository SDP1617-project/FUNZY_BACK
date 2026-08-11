package com.sdp1617.backend.letter.entity;

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
        name = "letter_interactions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_letter_interaction_once",
                columnNames = {"letter_id", "member_id", "type", "value"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LetterInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "letter_id", nullable = false)
    private Long letterId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LetterInteractionType type;

    @Column(length = 500)
    private String value;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public LetterInteraction(Long letterId, Long memberId, LetterInteractionType type, String value) {
        this.letterId = letterId;
        this.memberId = memberId;
        this.type = type;
        this.value = value;
        this.createdAt = LocalDateTime.now();
    }
}
