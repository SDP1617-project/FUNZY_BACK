package com.sdp1617.backend.letter.service;

import com.sdp1617.backend.letter.dto.ReceivedLetterListResponse;
import com.sdp1617.backend.letter.dto.ReceivedLetterResponse;
import com.sdp1617.backend.letter.entity.LetterSortType;
import com.sdp1617.backend.letter.entity.ReceivedLetter;
import com.sdp1617.backend.letter.repository.ReceivedLetterRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceivedLetterService {

    private final ReceivedLetterRepository receivedLetterRepository;

    public ReceivedLetterListResponse getReceivedLetters(
            Long memberId,
            LetterSortType sortType,
            String senderName,
            LocalDate receivedFrom,
            LocalDate receivedTo
    ) {
        Sort sort = switch (sortType) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "receivedAt");
            case OLDEST -> Sort.by(Sort.Direction.ASC, "receivedAt");
        };

        LocalDateTime from = receivedFrom == null ? null : receivedFrom.atStartOfDay();
        LocalDateTime to = receivedTo == null ? null : receivedTo.plusDays(1).atStartOfDay();

        return ReceivedLetterListResponse.from(
                receivedLetterRepository.findAll(spec(memberId, blankToNull(senderName), from, to), sort)
                        .stream()
                        .map(ReceivedLetterResponse::from)
                        .toList()
        );
    }

    private Specification<ReceivedLetter> spec(
            Long memberId,
            String senderName,
            LocalDateTime receivedFrom,
            LocalDateTime receivedTo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("receiverMemberId"), memberId));

            if (senderName != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("senderName")),
                        "%" + senderName.toLowerCase() + "%"
                ));
            }
            if (receivedFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("receivedAt"), receivedFrom));
            }
            if (receivedTo != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("receivedAt"), receivedTo));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
