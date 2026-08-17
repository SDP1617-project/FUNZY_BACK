package com.sdp1617.backend.letter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "편지 댓글 저장 요청")
public record LetterCommentRequest(
        @Schema(description = "댓글 내용", example = "편지 고마워!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 500)
        String content
) {
}
