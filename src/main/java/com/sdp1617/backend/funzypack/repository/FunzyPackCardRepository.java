package com.sdp1617.backend.funzypack.repository;

import com.sdp1617.backend.funzypack.entity.FunzyPackCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FunzyPackCardRepository extends JpaRepository<FunzyPackCard, Long> {

    List<FunzyPackCard> findByPackIdOrderByCardOrderAsc(Long packId);

    void deleteByPackId(Long packId);
}
