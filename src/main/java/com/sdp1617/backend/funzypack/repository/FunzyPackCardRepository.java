package com.sdp1617.backend.funzypack.repository;

import com.sdp1617.backend.funzypack.entity.FunzyPackCard;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FunzyPackCardRepository extends JpaRepository<FunzyPackCard, Long> {

    List<FunzyPackCard> findByPackIdOrderByCardOrderAsc(Long packId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select card from FunzyPackCard card where card.id = :id")
    Optional<FunzyPackCard> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select card from FunzyPackCard card where card.heartCardId = :heartCardId")
    Optional<FunzyPackCard> findByHeartCardIdForUpdate(@Param("heartCardId") Long heartCardId);

    void deleteByPackId(Long packId);
}
