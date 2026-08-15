package com.sdp1617.backend.letter.controller;

import com.sdp1617.backend.global.common.response.ApiResponse;
import com.sdp1617.backend.letter.dto.ReceivedLetterListResponse;
import com.sdp1617.backend.letter.entity.LetterSortType;
import com.sdp1617.backend.letter.service.ReceivedLetterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@Tag(name = "Received letters", description = "Received letter list, filter, and sorting API")
public class ReceivedLetterController {

    private final ReceivedLetterService receivedLetterService;

    @GetMapping("/api/letters/received")
    @Operation(
            summary = "Get received letters",
            description = "Loads received letters for the authenticated user with sender, date, sort, and pagination filters."
    )
    public ApiResponse<ReceivedLetterListResponse> getReceivedLetters(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Parameter(description = "Sort condition. LATEST or OLDEST.", example = "LATEST")
            @RequestParam(defaultValue = "LATEST") LetterSortType sort,
            @Parameter(description = "Sender name contains filter.", example = "Eunwoo")
            @RequestParam(required = false) String senderName,
            @Parameter(description = "Received date from.", example = "2026-08-01")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam(required = false) LocalDate receivedFrom,
            @Parameter(description = "Received date to.", example = "2026-08-31")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam(required = false) LocalDate receivedTo,
            @Parameter(description = "Page number, zero-based.", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size. Maximum is 100.", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        ReceivedLetterListResponse response = receivedLetterService.getReceivedLetters(
                memberId,
                sort,
                senderName,
                receivedFrom,
                receivedTo,
                page,
                size
        );
        return ApiResponse.ok("Received letters loaded.", response);
    }
}
