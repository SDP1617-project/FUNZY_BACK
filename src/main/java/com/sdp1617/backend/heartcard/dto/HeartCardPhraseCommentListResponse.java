package com.sdp1617.backend.heartcard.dto;

import com.sdp1617.backend.heartcard.entity.HeartCardPhraseComment;

import java.util.List;

public record HeartCardPhraseCommentListResponse(
        Long heartCardId,
        boolean empty,
        int count,
        List<HeartCardPhraseCommentResponse> comments
) {
    public static HeartCardPhraseCommentListResponse of(
            Long memberId,
            Long heartCardId,
            List<HeartCardPhraseComment> comments
    ) {
        List<HeartCardPhraseCommentResponse> responses = comments.stream()
                .map(comment -> HeartCardPhraseCommentResponse.from(comment, memberId))
                .toList();
        return new HeartCardPhraseCommentListResponse(heartCardId, responses.isEmpty(), responses.size(), responses);
    }
}
