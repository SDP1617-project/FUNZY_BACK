package com.sdp1617.backend.card.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CardImagePresignedUrlRequest(
        @NotBlank String fileName,
        @NotBlank String contentType
) {
}
