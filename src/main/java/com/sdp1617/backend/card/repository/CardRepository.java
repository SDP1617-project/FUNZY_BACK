package com.sdp1617.backend.card.repository;

import com.sdp1617.backend.card.entity.Card;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card,Long> {
    @EntityGraph(attributePaths = {"envelop", "envelop.sender", "envelop.receiver"})
    List<Card> findByEnvelop_Sender_IdOrderByCreatedAtDescIdDesc(Long senderId);

    @EntityGraph(attributePaths = {"envelop", "envelop.sender", "envelop.receiver"})
    List<Card> findByEnvelop_Receiver_IdOrderByCreatedAtDescIdDesc(Long receiverId);

    @EntityGraph(attributePaths = {"envelop", "envelop.sender", "envelop.receiver"})
    Optional<Card> findByIdAndEnvelop_Sender_Id(Long id, Long senderId);

    @EntityGraph(attributePaths = {"envelop", "envelop.sender", "envelop.receiver"})
    Optional<Card> findByIdAndEnvelop_Receiver_Id(Long id, Long receiverId);
}
