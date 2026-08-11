package com.sdp1617.backend.heartcard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "마음카드 선택 문구 코멘트 수정 요청")
public record HeartCardPhraseCommentUpdateRequest(
        @Schema(description = "수정할 코멘트 내용. 최대 50자입니다.", example = "수정한 코멘트", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 50)
        String content
) {
}
