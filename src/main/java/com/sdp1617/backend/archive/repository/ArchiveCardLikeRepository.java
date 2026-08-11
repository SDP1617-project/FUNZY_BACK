package com.sdp1617.backend.archive.repository;

import com.sdp1617.backend.archive.entity.ArchiveCardLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArchiveCardLikeRepository extends JpaRepository<ArchiveCardLike, Long> {

    boolean existsByArchiveCardIdAndMemberId(Long archiveCardId, Long memberId);

    Optional<ArchiveCardLike> findByArchiveCardIdAndMemberId(Long archiveCardId, Long memberId);

    void deleteByArchiveCardId(Long archiveCardId);
}
