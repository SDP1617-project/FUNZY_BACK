package com.sdp1617.backend.card.dto.request;

import com.sdp1617.backend.archive.entity.ArchiveCategory;
import com.sdp1617.backend.card.dto.DesignType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CardCreateRequest(
        @NotNull Long senderId,
        Long receiverId,
        DesignType designType,
        @NotBlank String title,
        @NotNull ArchiveCategory category,
        String link,
        String linkTitle,
        @NotBlank String content,
        String imageKey
) {
}
