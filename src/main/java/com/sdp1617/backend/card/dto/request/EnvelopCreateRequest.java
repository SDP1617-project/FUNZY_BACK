package com.sdp1617.backend.card.dto.request;

import com.sdp1617.backend.archive.entity.ArchiveCategory;
import com.sdp1617.backend.card.dto.DesignType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EnvelopCreateRequest(
        @NotNull Long senderId,
        @NotNull Long receiverId,
        @NotNull DesignType designType,
        @NotBlank String title,
        @NotNull ArchiveCategory category,
        String link,
        String linkTitle,
        @NotBlank String content
) {
}
