package com.sdp1617.backend.card.controller;

import com.sdp1617.backend.card.dto.request.EnvelopCreateRequest;
import com.sdp1617.backend.card.dto.response.CardResponse;
import com.sdp1617.backend.card.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "마음카드 API")
public class CardController {

    private final CardService cardService;

    @PostMapping("/create")
    @Operation(summary="마음카드 생성")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody EnvelopCreateRequest request){
        return ResponseEntity.ok(cardService.createCard(request));
    }
}
