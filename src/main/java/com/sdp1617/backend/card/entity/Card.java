package com.sdp1617.backend.card.entity;

import com.sdp1617.backend.card.dto.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Category category;

    @Column
    private String link;

    @Column(name="link_title")
    private String linkTitle;

    @Column(nullable=false)
    private String content;

    private Card(Envelop envelop, String title, Category category, String link, String linkTitle, String content) {
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
            Category category,
            String link,
            String linkTitle,
            String content
    ) {
        return new Card(envelop, title, category, link, linkTitle, content);
    }
}
