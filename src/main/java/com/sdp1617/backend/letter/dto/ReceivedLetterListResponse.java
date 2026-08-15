package com.sdp1617.backend.letter.dto;

import java.util.List;

public record ReceivedLetterListResponse(
        boolean empty,
        int count,
        List<ReceivedLetterResponse> letters,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static ReceivedLetterListResponse from(List<ReceivedLetterResponse> letters) {
        return new ReceivedLetterListResponse(letters.isEmpty(), letters.size(), letters, 0, letters.size(), letters.size(), 1, true, true);
    }

    public static ReceivedLetterListResponse of(
            List<ReceivedLetterResponse> letters,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {
        return new ReceivedLetterListResponse(letters.isEmpty(), letters.size(), letters, page, size, totalElements, totalPages, first, last);
    }
}
