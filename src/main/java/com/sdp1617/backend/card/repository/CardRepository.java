package com.sdp1617.backend.card.repository;

import com.sdp1617.backend.card.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card,Long> {
}
