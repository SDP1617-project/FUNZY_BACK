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
@Tag(name = "받은 편지함", description = "받은 편지 목록 조회, 필터, 정렬 API")
public class ReceivedLetterController {

    private final ReceivedLetterService receivedLetterService;

    @GetMapping("/api/letters/received")
    @Operation(
            summary = "받은 편지 목록 조회",
            description = "로그인한 사용자가 받은 편지를 조회합니다. 기본 정렬은 최신순이며, 발신자명/수신일 기간/정렬 조건을 함께 적용할 수 있습니다."
    )
    public ApiResponse<ReceivedLetterListResponse> getReceivedLetters(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Parameter(description = "정렬 조건. LATEST=최신순, OLDEST=오래된순", example = "LATEST")
            @RequestParam(defaultValue = "LATEST") LetterSortType sort,
            @Parameter(description = "상대 이름 필터. 발신자명에 포함되는 값으로 검색합니다.", example = "Eunwoo")
            @RequestParam(required = false) String senderName,
            @Parameter(description = "조회 시작 수신일", example = "2026-08-01")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam(required = false) LocalDate receivedFrom,
            @Parameter(description = "조회 종료 수신일", example = "2026-08-31")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam(required = false) LocalDate receivedTo
    ) {
        ReceivedLetterListResponse response = receivedLetterService.getReceivedLetters(
                memberId,
                sort,
                senderName,
                receivedFrom,
                receivedTo
        );
        return ApiResponse.ok("Received letters loaded.", response);
    }
}
