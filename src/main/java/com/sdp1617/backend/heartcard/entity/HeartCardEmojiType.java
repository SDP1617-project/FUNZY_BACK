package com.sdp1617.backend.heartcard.entity;

public enum HeartCardEmojiType {
    HEART("하트"),
    LIKE("좋아요"),
    HAPPY("기뻐요"),
    SAD("슬퍼요"),
    SURPRISED("놀라워요"),
    FUNNY("웃겨요");

    private final String label;

    HeartCardEmojiType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
