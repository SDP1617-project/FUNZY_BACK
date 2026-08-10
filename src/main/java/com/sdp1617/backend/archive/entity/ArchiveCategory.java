package com.sdp1617.backend.archive.entity;

public enum ArchiveCategory {
    BOOK("책"),
    MOVIE_TV("영화/TV"),
    MUSIC("음악"),
    FASHION("패션"),
    PLACE("장소"),
    ETC("기타");

    private final String displayName;

    ArchiveCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
