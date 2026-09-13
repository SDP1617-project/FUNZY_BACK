package com.sdp1617.backend.mypage.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfileImagePresignedUrlRequest(
        @NotBlank String fileName,
        @NotBlank String contentType
) {
}
