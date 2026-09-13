package com.sdp1617.backend.card.dto.response;

import com.sdp1617.backend.card.dto.CardBoxType;

import java.util.List;

public record CardCalendarResponse(
        CardBoxType type,
        int year,
        int month,
        List<CardCalendarDayResponse> days
) {
}
