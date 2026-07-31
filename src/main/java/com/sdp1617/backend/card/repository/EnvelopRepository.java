package com.sdp1617.backend.card.repository;

import com.sdp1617.backend.card.entity.Envelop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnvelopRepository extends JpaRepository<Envelop,Long> {
    boolean existsBySender_Id(Long senderId);

    Optional<Envelop> findBySender_Id(Long senderId);
}
