package com.sdp1617.backend.heartcard.dto;

import com.sdp1617.backend.heartcard.entity.HeartCardPhraseComment;

import java.time.LocalDateTime;

public record HeartCardPhraseCommentResponse(
        Long commentId,
        Long heartCardId,
        Long memberId,
        int startOffset,
        int endOffset,
        String selectedText,
        String content,
        boolean mine,
        boolean notificationCreated,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static HeartCardPhraseCommentResponse from(HeartCardPhraseComment comment, Long viewerMemberId) {
        return of(comment, viewerMemberId, false);
    }

    public static HeartCardPhraseCommentResponse of(
            HeartCardPhraseComment comment,
            Long viewerMemberId,
            boolean notificationCreated
    ) {
        return new HeartCardPhraseCommentResponse(
                comment.getId(),
                comment.getHeartCardId(),
                comment.getMemberId(),
                comment.getStartOffset(),
                comment.getEndOffset(),
                comment.getSelectedText(),
                comment.getContent(),
                comment.isWrittenBy(viewerMemberId),
                notificationCreated,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
