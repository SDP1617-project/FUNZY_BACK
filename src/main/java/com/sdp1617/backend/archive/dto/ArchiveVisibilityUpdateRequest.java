package com.sdp1617.backend.archive.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "아카이브 카드 상세 항목별 공개 범위 수정 요청 값")
public record ArchiveVisibilityUpdateRequest(
        @Schema(description = "카드 발신자 공개 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean senderVisible,

        @Schema(description = "카드 수신자 공개 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean receiverVisible,

        @Schema(description = "카드 날짜 공개 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean dateVisible,

        @Schema(description = "카드 대표 이미지 공개 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean imageVisible,

        @Schema(description = "카드 메시지 공개 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean messageVisible
) {
}
