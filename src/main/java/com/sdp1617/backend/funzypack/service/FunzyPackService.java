package com.sdp1617.backend.funzypack.service;

import com.sdp1617.backend.archive.repository.ArchiveCardRepository;
import com.sdp1617.backend.funzypack.dto.FunzyPackCardListResponse;
import com.sdp1617.backend.funzypack.entity.FunzyPackCard;
import com.sdp1617.backend.funzypack.repository.FunzyPackCardRepository;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import com.sdp1617.backend.heartcard.repository.HeartCardEmojiReactionRepository;
import com.sdp1617.backend.heartcard.repository.HeartCardPhraseCommentRepository;
import com.sdp1617.backend.letter.repository.ReceivedLetterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FunzyPackService {

    private final ReceivedLetterRepository receivedLetterRepository;
    private final FunzyPackCardRepository funzyPackCardRepository;
    private final ArchiveCardRepository archiveCardRepository;
    private final HeartCardEmojiReactionRepository heartCardEmojiReactionRepository;
    private final HeartCardPhraseCommentRepository heartCardPhraseCommentRepository;

    public FunzyPackCardListResponse getCards(Long memberId, Long packId) {
        requireLogin(memberId);
        validateOwner(memberId, packId);
        return FunzyPackCardListResponse.of(packId, funzyPackCardRepository.findByPackIdOrderByCardOrderAsc(packId));
    }

    @Transactional
    public void deletePack(Long memberId, Long packId) {
        requireLogin(memberId);
        validateOwner(memberId, packId);

        List<Long> heartCardIds = funzyPackCardRepository.findByPackIdOrderByCardOrderAsc(packId)
                .stream()
                .map(FunzyPackCard::getHeartCardId)
                .toList();

        if (!heartCardIds.isEmpty()) {
            archiveCardRepository.deleteByOwnerMemberIdAndLetterCardIdIn(memberId, heartCardIds);
            heartCardEmojiReactionRepository.deleteByHeartCardIdIn(heartCardIds);
            heartCardPhraseCommentRepository.deleteByHeartCardIdIn(heartCardIds);
        }

        funzyPackCardRepository.deleteByPackId(packId);
        receivedLetterRepository.deleteById(packId);
    }

    private void validateOwner(Long memberId, Long packId) {
        if (!receivedLetterRepository.existsByIdAndReceiverMemberId(packId, memberId)) {
            throw new CustomException(ErrorCode.COMMON_001);
        }
    }

    private void requireLogin(Long memberId) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.COMMON_003);
        }
    }
}
