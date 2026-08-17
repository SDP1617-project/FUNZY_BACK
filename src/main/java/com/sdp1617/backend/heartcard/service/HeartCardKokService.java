package com.sdp1617.backend.heartcard.service;

import com.sdp1617.backend.archive.entity.ArchiveCard;
import com.sdp1617.backend.archive.repository.ArchiveCardLikeRepository;
import com.sdp1617.backend.archive.repository.ArchiveCardRepository;
import com.sdp1617.backend.funzypack.entity.FunzyPackCard;
import com.sdp1617.backend.funzypack.repository.FunzyPackCardRepository;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import com.sdp1617.backend.heartcard.dto.HeartCardKokRequest;
import com.sdp1617.backend.heartcard.dto.HeartCardKokResponse;
import com.sdp1617.backend.letter.repository.ReceivedLetterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeartCardKokService {

    private final ArchiveCardRepository archiveCardRepository;
    private final ArchiveCardLikeRepository archiveCardLikeRepository;
    private final FunzyPackCardRepository funzyPackCardRepository;
    private final ReceivedLetterRepository receivedLetterRepository;

    public HeartCardKokResponse getKok(Long memberId, Long heartCardId) {
        requireLogin(memberId);
        return archiveCardRepository.findByOwnerMemberIdAndLetterCardId(memberId, heartCardId)
                .map(archiveCard -> HeartCardKokResponse.active(heartCardId, archiveCard))
                .orElseGet(() -> HeartCardKokResponse.inactive(heartCardId));
    }

    @Transactional
    public HeartCardKokResponse updateKok(Long memberId, Long heartCardId, HeartCardKokRequest request) {
        requireLogin(memberId);
        HeartCardKokRequest safeRequest = request == null ? new HeartCardKokRequest(true, null) : request;
        return archiveCardRepository.findByOwnerMemberIdAndLetterCardId(memberId, heartCardId)
                .map(archiveCard -> safeRequest.kokOrDefault()
                        ? HeartCardKokResponse.active(heartCardId, archiveCard)
                        : deleteKok(heartCardId, archiveCard))
                .orElseGet(() -> safeRequest.kokOrDefault()
                        ? createKok(memberId, heartCardId, safeRequest)
                        : HeartCardKokResponse.inactive(heartCardId));
    }

    private HeartCardKokResponse createKok(Long memberId, Long heartCardId, HeartCardKokRequest request) {
        FunzyPackCard targetCard = funzyPackCardRepository.findByHeartCardIdForUpdate(heartCardId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_001));
        if (!receivedLetterRepository.existsByIdAndReceiverMemberId(targetCard.getPackId(), memberId)) {
            throw new CustomException(ErrorCode.COMMON_001);
        }

        ArchiveCard archiveCard = archiveCardRepository.save(new ArchiveCard(
                memberId,
                heartCardId,
                request.categoryOrDefault()
        ));
        return HeartCardKokResponse.active(heartCardId, archiveCard);
    }

    private HeartCardKokResponse deleteKok(Long heartCardId, ArchiveCard archiveCard) {
        archiveCardLikeRepository.deleteByArchiveCardId(archiveCard.getId());
        archiveCardRepository.delete(archiveCard);
        return HeartCardKokResponse.inactive(heartCardId);
    }

    private void requireLogin(Long memberId) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.COMMON_003);
        }
    }
}
