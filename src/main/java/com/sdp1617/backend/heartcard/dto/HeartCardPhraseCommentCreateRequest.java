package com.sdp1617.backend.heartcard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "마음카드 선택 문구 코멘트 작성 요청")
public record HeartCardPhraseCommentCreateRequest(
        @Schema(description = "선택 문구 시작 위치. 본문 첫 글자는 0입니다.", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(0) int startOffset,

        @Schema(description = "선택 문구 끝 위치. startOffset보다 커야 하며 endOffset 글자는 포함하지 않습니다.", example = "18", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(1) int endOffset,

        @Schema(description = "선택한 문구 텍스트", example = "보고 싶었어", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 500)
        String selectedText,

        @Schema(description = "코멘트 내용. 최대 50자입니다.", example = "나도 이 부분 좋았어", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 50)
        String content
) {
}
