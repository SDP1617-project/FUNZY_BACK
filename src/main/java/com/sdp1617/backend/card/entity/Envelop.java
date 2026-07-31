package com.sdp1617.backend.card.entity;

import com.sdp1617.backend.auth.entity.User;
import com.sdp1617.backend.card.dto.DesignType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Envelop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "design_type",nullable = false)
    @Enumerated(EnumType.STRING)
    private DesignType designType;

    private Envelop(User sender, User receiver, DesignType designType) {
        this.sender = sender;
        this.receiver = receiver;
        this.designType = designType;
    }

    public static Envelop create(User sender, User receiver, DesignType designType) {
        return new Envelop(sender, receiver, designType);
    }
}
