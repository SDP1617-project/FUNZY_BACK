package com.sdp1617.backend.letter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "received_letters")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReceivedLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receiver_member_id", nullable = false)
    private Long receiverMemberId;

    @Column(name = "sender_member_id")
    private Long senderMemberId;

    @Column(nullable = false, length = 100)
    private String senderName;

    @Column(nullable = false, length = 100)
    private String receiverName;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    @Column(nullable = false)
    private int heartCardCount;

    public ReceivedLetter(
            Long receiverMemberId,
            Long senderMemberId,
            String senderName,
            String receiverName,
            LocalDateTime receivedAt,
            int heartCardCount
    ) {
        this.receiverMemberId = receiverMemberId;
        this.senderMemberId = senderMemberId;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.receivedAt = receivedAt;
        this.heartCardCount = heartCardCount;
    }
}
