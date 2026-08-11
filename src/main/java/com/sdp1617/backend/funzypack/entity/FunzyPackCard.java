package com.sdp1617.backend.funzypack.entity;

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

@Getter
@Entity
@Table(
        name = "funzy_pack_cards",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_funzy_pack_card_order",
                columnNames = {"pack_id", "card_order"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FunzyPackCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pack_id", nullable = false)
    private Long packId;

    @Column(name = "heart_card_id", nullable = false)
    private Long heartCardId;

    @Column(name = "card_order", nullable = false)
    private int cardOrder;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 2000)
    private String message;

    public FunzyPackCard(Long packId, Long heartCardId, int cardOrder, String imageUrl, String message) {
        this.packId = packId;
        this.heartCardId = heartCardId;
        this.cardOrder = cardOrder;
        this.imageUrl = imageUrl;
        this.message = message;
    }
}
