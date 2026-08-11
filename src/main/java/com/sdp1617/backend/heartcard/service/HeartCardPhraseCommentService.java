package com.sdp1617.backend.heartcard.service;

import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import com.sdp1617.backend.heartcard.dto.HeartCardPhraseCommentCreateRequest;
import com.sdp1617.backend.heartcard.dto.HeartCardPhraseCommentListResponse;
import com.sdp1617.backend.heartcard.dto.HeartCardPhraseCommentResponse;
import com.sdp1617.backend.heartcard.dto.HeartCardPhraseCommentUpdateRequest;
import com.sdp1617.backend.heartcard.entity.HeartCardPhraseComment;
import com.sdp1617.backend.heartcard.repository.HeartCardPhraseCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeartCardPhraseCommentService {

    private final HeartCardPhraseCommentRepository heartCardPhraseCommentRepository;

    public HeartCardPhraseCommentListResponse getComments(Long memberId, Long heartCardId) {
        requireLogin(memberId);
        return HeartCardPhraseCommentListResponse.of(
                memberId,
                heartCardId,
                heartCardPhraseCommentRepository.findByHeartCardIdOrderByStartOffsetAsc(heartCardId)
        );
    }

    public HeartCardPhraseCommentResponse getComment(Long memberId, Long heartCardId, Long commentId) {
        requireLogin(memberId);
        return HeartCardPhraseCommentResponse.from(findComment(heartCardId, commentId), memberId);
    }

    @Transactional
    public HeartCardPhraseCommentResponse createComment(
            Long memberId,
            Long heartCardId,
            HeartCardPhraseCommentCreateRequest request
    ) {
        requireLogin(memberId);
        validateRange(request.startOffset(), request.endOffset());
        if (heartCardPhraseCommentRepository.existsOverlappingRange(heartCardId, request.startOffset(), request.endOffset())) {
            throw new CustomException(ErrorCode.COMMON_002);
        }

        HeartCardPhraseComment comment = heartCardPhraseCommentRepository.save(new HeartCardPhraseComment(
                heartCardId,
                memberId,
                request.startOffset(),
                request.endOffset(),
                request.selectedText(),
                request.content()
        ));
        // TODO: 알림 도메인(LN-022)이 연결되면 코멘트 등록 시에만 알림 INBOX 생성 호출.
        return HeartCardPhraseCommentResponse.of(comment, memberId, true);
    }

    @Transactional
    public HeartCardPhraseCommentResponse updateComment(
            Long memberId,
            Long heartCardId,
            Long commentId,
            HeartCardPhraseCommentUpdateRequest request
    ) {
        requireLogin(memberId);
        HeartCardPhraseComment comment = findComment(heartCardId, commentId);
        validateOwner(comment, memberId);
        comment.updateContent(request.content());
        return HeartCardPhraseCommentResponse.from(comment, memberId);
    }

    @Transactional
    public void deleteComment(Long memberId, Long heartCardId, Long commentId) {
        requireLogin(memberId);
        HeartCardPhraseComment comment = findComment(heartCardId, commentId);
        validateOwner(comment, memberId);
        heartCardPhraseCommentRepository.delete(comment);
    }

    private HeartCardPhraseComment findComment(Long heartCardId, Long commentId) {
        return heartCardPhraseCommentRepository.findByIdAndHeartCardId(commentId, heartCardId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_001));
    }

    private void validateRange(int startOffset, int endOffset) {
        if (startOffset >= endOffset) {
            throw new CustomException(ErrorCode.COMMON_002);
        }
    }

    private void validateOwner(HeartCardPhraseComment comment, Long memberId) {
        if (!comment.isWrittenBy(memberId)) {
            throw new CustomException(ErrorCode.COMMON_004);
        }
    }

    private void requireLogin(Long memberId) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.COMMON_003);
        }
    }
}
