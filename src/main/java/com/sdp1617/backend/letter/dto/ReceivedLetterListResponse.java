package com.sdp1617.backend.letter.dto;

import java.util.List;

public record ReceivedLetterListResponse(
        boolean empty,
        int count,
        List<ReceivedLetterResponse> letters
) {
    public static ReceivedLetterListResponse from(List<ReceivedLetterResponse> letters) {
        return new ReceivedLetterListResponse(letters.isEmpty(), letters.size(), letters);
    }
}
