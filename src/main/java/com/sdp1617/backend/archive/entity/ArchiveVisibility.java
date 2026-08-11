package com.sdp1617.backend.archive.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArchiveVisibility {

    private boolean senderVisible = true;
    private boolean receiverVisible = true;
    private boolean dateVisible = true;
    private boolean imageVisible = true;
    private boolean messageVisible = true;

    public ArchiveVisibility(
            boolean senderVisible,
            boolean receiverVisible,
            boolean dateVisible,
            boolean imageVisible,
            boolean messageVisible
    ) {
        this.senderVisible = senderVisible;
        this.receiverVisible = receiverVisible;
        this.dateVisible = dateVisible;
        this.imageVisible = imageVisible;
        this.messageVisible = messageVisible;
    }
}
