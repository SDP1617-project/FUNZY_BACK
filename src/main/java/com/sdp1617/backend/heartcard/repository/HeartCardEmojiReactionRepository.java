package com.sdp1617.backend.heartcard.repository;

import com.sdp1617.backend.heartcard.entity.HeartCardEmojiReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HeartCardEmojiReactionRepository extends JpaRepository<HeartCardEmojiReaction, Long> {

    Optional<HeartCardEmojiReaction> findByHeartCardIdAndMemberId(Long heartCardId, Long memberId);
}
