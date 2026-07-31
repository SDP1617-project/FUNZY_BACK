package com.sdp1617.backend.card.service;

import com.sdp1617.backend.auth.entity.User;
import com.sdp1617.backend.card.dto.request.EnvelopCreateRequest;
import com.sdp1617.backend.card.dto.response.CardResponse;
import com.sdp1617.backend.card.entity.Card;
import com.sdp1617.backend.card.entity.Envelop;
import com.sdp1617.backend.card.repository.CardRepository;
import com.sdp1617.backend.card.repository.EnvelopRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final EnvelopRepository envelopRepository;
    private final CardRepository cardRepository;
    private final EntityManager entityManager;

    @Transactional
    public CardResponse createCard(EnvelopCreateRequest request){
        Envelop envelop;

        if (!envelopRepository.existsBySender_Id(request.senderId())) {
            envelop = createEnvelop(request);
        } else {
            envelop = envelopRepository.findBySender_Id(request.senderId())
                    .orElseThrow();
        }

        Card card = Card.create(
                envelop,
                request.title(),
                request.category(),
                request.link(),
                request.linkTitle(),
                request.content()
        );
        Card savedCard = cardRepository.save(card);

        return CardResponse.from(savedCard);
    }

    private Envelop createEnvelop(EnvelopCreateRequest request) {
        User sender = entityManager.getReference(User.class, request.senderId());
        User receiver = entityManager.getReference(User.class, request.receiverId());

        Envelop envelop = Envelop.create(sender, receiver, request.designType());
        return envelopRepository.save(envelop);
    }
}
