package com.sdp1617.backend.card.entity;

import com.sdp1617.backend.archive.entity.ArchiveCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envelop_id", nullable = false)
    private Envelop envelop;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ArchiveCategory category;

    @Column
    private String link;

    @Column(name="link_title")
    private String linkTitle;

    @Column(nullable=false)
    private String content;

    @Column(name = "image_key", length = 500)
    private String imageKey;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private Card(Envelop envelop, String title, ArchiveCategory category, String link, String linkTitle, String content) {
        this.envelop = envelop;
        this.title = title;
        this.category = category;
        this.link = link;
        this.linkTitle = linkTitle;
        this.content = content;
    }

    public static Card create(
            Envelop envelop,
            String title,
            ArchiveCategory category,
            String link,
            String linkTitle,
            String content
    ) {
        return new Card(envelop, title, category, link, linkTitle, content);
    }

    public void updateImage(String imageKey, String imageUrl) {
        this.imageKey = imageKey;
        this.imageUrl = imageUrl;
    }

    @PrePersist
    private void assignCreatedAt() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
