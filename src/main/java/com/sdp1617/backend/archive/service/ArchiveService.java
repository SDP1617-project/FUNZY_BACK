package com.sdp1617.backend.archive.service;

import com.sdp1617.backend.archive.dto.ArchiveCardCreateRequest;
import com.sdp1617.backend.archive.dto.ArchiveCardDetailResponse;
import com.sdp1617.backend.archive.dto.ArchiveCardResponse;
import com.sdp1617.backend.archive.dto.ArchiveCategorySectionResponse;
import com.sdp1617.backend.archive.dto.ArchiveHomeResponse;
import com.sdp1617.backend.archive.dto.ArchiveLikeResponse;
import com.sdp1617.backend.archive.dto.ArchiveVisibilityResponse;
import com.sdp1617.backend.archive.dto.ArchiveVisibilityUpdateRequest;
import com.sdp1617.backend.archive.entity.ArchiveCard;
import com.sdp1617.backend.archive.entity.ArchiveCategory;
import com.sdp1617.backend.archive.entity.ArchiveVisibility;
import com.sdp1617.backend.archive.entity.ArchiveCardLike;
import com.sdp1617.backend.archive.repository.ArchiveCardLikeRepository;
import com.sdp1617.backend.archive.repository.ArchiveCardRepository;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArchiveService {

    private static final String ARCHIVE_TITLE = "아카이브";

    private final ArchiveCardRepository archiveCardRepository;
    private final ArchiveCardLikeRepository archiveCardLikeRepository;

    public ArchiveHomeResponse getHome(Long memberId) {
        List<ArchiveCategorySectionResponse> sections = Arrays.stream(ArchiveCategory.values())
                .map(category -> ArchiveCategorySectionResponse.of(
                        category,
                        archiveCardRepository.findByOwnerMemberIdAndCategoryOrderByCreatedAtDesc(memberId, category)
                                .stream()
                                .map(ArchiveCardResponse::from)
                                .toList()
                ))
                .toList();
        boolean empty = sections.stream().allMatch(ArchiveCategorySectionResponse::empty);

        return new ArchiveHomeResponse(memberId, ARCHIVE_TITLE, null, empty, sections);
    }

    @Transactional
    public ArchiveCardDetailResponse saveCard(Long memberId, ArchiveCardCreateRequest request) {
        if (archiveCardRepository.existsByOwnerMemberIdAndLetterCardId(memberId, request.letterCardId())) {
            throw new CustomException(ErrorCode.ARCHIVE_001);
        }

        ArchiveCard card = archiveCardRepository.save(new ArchiveCard(memberId, request.letterCardId(), request.category()));
        return ArchiveCardDetailResponse.from(card, false, false);
    }

    public ArchiveCardDetailResponse getCard(Long viewerMemberId, Long archiveCardId, boolean friendView) {
        ArchiveCard card = findCard(archiveCardId);
        boolean maskPrivateFields = friendView && !card.isOwnedBy(viewerMemberId);
        boolean liked = archiveCardLikeRepository.existsByArchiveCardIdAndMemberId(archiveCardId, viewerMemberId);
        return ArchiveCardDetailResponse.from(card, maskPrivateFields, liked);
    }

    @Transactional
    public void deleteCard(Long memberId, Long archiveCardId) {
        ArchiveCard card = archiveCardRepository.findByIdAndOwnerMemberId(archiveCardId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARCHIVE_002));
        archiveCardLikeRepository.deleteByArchiveCardId(archiveCardId);
        archiveCardRepository.delete(card);
    }

    @Transactional
    public ArchiveVisibilityResponse updateVisibility(
            Long memberId,
            Long archiveCardId,
            ArchiveVisibilityUpdateRequest request
    ) {
        ArchiveCard card = archiveCardRepository.findByIdAndOwnerMemberId(archiveCardId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARCHIVE_002));
        card.updateVisibility(new ArchiveVisibility(
                request.senderVisible(),
                request.receiverVisible(),
                request.dateVisible(),
                request.imageVisible(),
                request.messageVisible()
        ));
        return ArchiveVisibilityResponse.from(card);
    }

    @Transactional
    public ArchiveLikeResponse toggleLike(Long memberId, Long archiveCardId) {
        ArchiveCard card = findCard(archiveCardId);
        if (card.isOwnedBy(memberId)) {
            throw new CustomException(ErrorCode.ARCHIVE_003);
        }

        return archiveCardLikeRepository.findByArchiveCardIdAndMemberId(archiveCardId, memberId)
                .map(like -> unlike(card, like))
                .orElseGet(() -> like(card, memberId));
    }

    private ArchiveLikeResponse like(ArchiveCard card, Long memberId) {
        archiveCardLikeRepository.save(new ArchiveCardLike(card.getId(), memberId));
        card.increaseLikeCount();
        return new ArchiveLikeResponse(card.getId(), card.getLikeCount(), true);
    }

    private ArchiveLikeResponse unlike(ArchiveCard card, ArchiveCardLike like) {
        archiveCardLikeRepository.delete(like);
        card.decreaseLikeCount();
        return new ArchiveLikeResponse(card.getId(), card.getLikeCount(), false);
    }

    private ArchiveCard findCard(Long archiveCardId) {
        return archiveCardRepository.findById(archiveCardId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARCHIVE_002));
    }
}
