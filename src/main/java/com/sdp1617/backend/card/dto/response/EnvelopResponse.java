package com.sdp1617.backend.card.dto.response;

import com.sdp1617.backend.card.entity.Envelop;

public record EnvelopResponse (
        Long envelopId
){
    public static EnvelopResponse from(Envelop envelop) {
        return new EnvelopResponse(envelop.getId());
    }
}
