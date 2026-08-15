package com.sdp1617.backend.giftitem.service;

import com.sdp1617.backend.funzypack.entity.FunzyPackCard;
import com.sdp1617.backend.funzypack.repository.FunzyPackCardRepository;
import com.sdp1617.backend.giftitem.dto.GiftItemEmojiRequest;
import com.sdp1617.backend.giftitem.dto.GiftItemEmojiResponse;
import com.sdp1617.backend.giftitem.entity.GiftItemEmojiReaction;
import com.sdp1617.backend.giftitem.repository.GiftItemEmojiReactionRepository;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import com.sdp1617.backend.heartcard.entity.HeartCardEmojiAction;
import com.sdp1617.backend.letter.repository.ReceivedLetterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GiftItemEmojiService {

    private final GiftItemEmojiReactionRepository giftItemEmojiReactionRepository;
    private final FunzyPackCardRepository funzyPackCardRepository;
    private final ReceivedLetterRepository receivedLetterRepository;

    public GiftItemEmojiResponse getMyEmoji(Long memberId, Long giftItemId) {
        requireLogin(memberId);
        validateGiftItemAccess(memberId, giftItemId);
        return giftItemEmojiReactionRepository.findByGiftItemIdAndMemberId(giftItemId, memberId)
                .map(GiftItemEmojiResponse::from)
                .orElseGet(() -> GiftItemEmojiResponse.empty(giftItemId));
    }

    @Transactional
    public GiftItemEmojiResponse updateEmoji(Long memberId, Long giftItemId, GiftItemEmojiRequest request) {
        requireLogin(memberId);
        FunzyPackCard giftItem = findGiftItemForUpdate(memberId, giftItemId);
        return giftItemEmojiReactionRepository.findByGiftItemIdAndMemberId(giftItemId, memberId)
                .map(reaction -> updateOrDelete(giftItem.getId(), reaction, request))
                .orElseGet(() -> create(memberId, giftItem.getId(), request));
    }

    private GiftItemEmojiResponse create(Long memberId, Long giftItemId, GiftItemEmojiRequest request) {
        GiftItemEmojiReaction reaction = giftItemEmojiReactionRepository.save(
                new GiftItemEmojiReaction(giftItemId, memberId, request.emoji())
        );
        // TODO: 알림 도메인이 연결되면 최초 등록 시에만 보낸 사람 알림 INBOX 생성 호출.
        return GiftItemEmojiResponse.of(reaction.getGiftItemId(), reaction.getEmoji(), HeartCardEmojiAction.CREATED, false);
    }

    private GiftItemEmojiResponse updateOrDelete(
            Long giftItemId,
            GiftItemEmojiReaction reaction,
            GiftItemEmojiRequest request
    ) {
        if (reaction.getEmoji() == request.emoji()) {
            giftItemEmojiReactionRepository.delete(reaction);
            return GiftItemEmojiResponse.of(giftItemId, null, HeartCardEmojiAction.DELETED, false);
        }

        reaction.updateEmoji(request.emoji());
        return GiftItemEmojiResponse.of(giftItemId, reaction.getEmoji(), HeartCardEmojiAction.UPDATED, false);
    }

    private void requireLogin(Long memberId) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.COMMON_003);
        }
    }

    private void validateGiftItemAccess(Long memberId, Long giftItemId) {
        FunzyPackCard giftItem = funzyPackCardRepository.findById(giftItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_001));
        validateOwner(memberId, giftItem);
    }

    private FunzyPackCard findGiftItemForUpdate(Long memberId, Long giftItemId) {
        FunzyPackCard giftItem = funzyPackCardRepository.findByIdForUpdate(giftItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_001));
        validateOwner(memberId, giftItem);
        return giftItem;
    }

    private void validateOwner(Long memberId, FunzyPackCard giftItem) {
        if (!receivedLetterRepository.existsByIdAndReceiverMemberId(giftItem.getPackId(), memberId)) {
            throw new CustomException(ErrorCode.COMMON_004);
        }
    }
}
