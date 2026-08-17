package com.sdp1617.backend.giftitem.dto;

import com.sdp1617.backend.heartcard.entity.HeartCardEmojiType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "선물 항목 이모지 등록/수정/삭제 요청")
public record GiftItemEmojiRequest(
        @Schema(
                description = "선택한 이모지. 기존 이모지와 같은 값을 보내면 삭제됩니다.",
                example = "HEART",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"HEART", "LIKE", "HAPPY", "SAD", "SURPRISED", "FUNNY"}
        )
        @NotNull HeartCardEmojiType emoji
) {
}
