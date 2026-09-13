package com.sdp1617.backend.card.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CardImageUploadCompleteRequest(
        @NotBlank String imageKey
) {
}
