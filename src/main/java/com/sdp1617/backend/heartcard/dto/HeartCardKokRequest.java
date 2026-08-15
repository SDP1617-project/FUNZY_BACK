package com.sdp1617.backend.heartcard.dto;

import com.sdp1617.backend.archive.entity.ArchiveCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Heart card kok update request.")
public record HeartCardKokRequest(
        @Schema(description = "Target kok state. true stores the card, false removes it.", example = "true")
        Boolean kok,

        @Schema(
                description = "Archive category. Defaults to ETC when omitted.",
                example = "BOOK",
                allowableValues = {"BOOK", "MOVIE_TV", "MUSIC", "FASHION", "PLACE", "ETC"}
        )
        ArchiveCategory category
) {
    public HeartCardKokRequest(ArchiveCategory category) {
        this(true, category);
    }

    public boolean kokOrDefault() {
        return kok == null || kok;
    }

    public ArchiveCategory categoryOrDefault() {
        return category == null ? ArchiveCategory.ETC : category;
    }
}
