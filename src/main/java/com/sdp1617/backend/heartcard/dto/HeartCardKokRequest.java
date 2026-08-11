package com.sdp1617.backend.heartcard.dto;

import com.sdp1617.backend.archive.entity.ArchiveCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마음카드 콕 저장 요청. 콕 해제 시에는 값이 사용되지 않습니다.")
public record HeartCardKokRequest(
        @Schema(
                description = "아카이브 저장 카테고리. 생략하면 ETC로 저장합니다.",
                example = "BOOK",
                allowableValues = {"BOOK", "MOVIE_TV", "MUSIC", "FASHION", "PLACE", "ETC"}
        )
        ArchiveCategory category
) {
    public ArchiveCategory categoryOrDefault() {
        return category == null ? ArchiveCategory.ETC : category;
    }
}
