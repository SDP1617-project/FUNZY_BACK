package com.sdp1617.backend.social.controller;

import com.sdp1617.backend.global.common.response.ApiResponse;
import com.sdp1617.backend.social.dto.FollowCodeResponse;
import com.sdp1617.backend.social.dto.FollowCountResponse;
import com.sdp1617.backend.social.dto.FollowRequestCreateRequest;
import com.sdp1617.backend.social.dto.FollowRequestResponse;
import com.sdp1617.backend.social.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/social/follow")
@Tag(name = "소셜 - 팔로우", description = "고유 코드 발급/재발급, 팔로우 요청/수락/거절/끊기, 친구 수 조회 API")
public class FollowController {

    private final FollowService followService;

    @GetMapping("/code")
    @Operation(summary = "내 팔로우 코드 조회")
    public ApiResponse<FollowCodeResponse> getMyFollowCode(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.ok("팔로우 코드를 조회했습니다.", followService.getMyFollowCode(memberId));
    }

    @PostMapping("/code/reissue")
    @Operation(summary = "팔로우 코드 재발급", description = "재발급 시 기존 코드는 즉시 무효화됩니다.")
    public ApiResponse<FollowCodeResponse> reissueFollowCode(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.ok("팔로우 코드가 재발급되었습니다.", followService.reissueFollowCode(memberId));
    }

    @PostMapping("/requests")
    @Operation(summary = "팔로우 코드로 팔로우 요청", description = "상대가 이미 나에게 요청을 보낸 상태라면 바로 맞팔로 처리됩니다.")
    public ApiResponse<Void> sendFollowRequest(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody FollowRequestCreateRequest request
    ) {
        followService.sendFollowRequest(memberId, request.followCode());
        return ApiResponse.created("팔로우 요청을 보냈습니다.", null);
    }

    @GetMapping("/requests/received")
    @Operation(summary = "받은 팔로우 요청 목록 조회")
    public ApiResponse<List<FollowRequestResponse>> getReceivedRequests(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.ok("받은 팔로우 요청 목록을 조회했습니다.", followService.getReceivedRequests(memberId));
    }

    @PostMapping("/requests/{requestId}/accept")
    @Operation(summary = "팔로우 요청 수락")
    public ApiResponse<Void> acceptFollowRequest(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @PathVariable Long requestId
    ) {
        followService.acceptFollowRequest(memberId, requestId);
        return ApiResponse.ok("팔로우 요청을 수락했습니다.", null);
    }

    @PostMapping("/requests/{requestId}/reject")
    @Operation(summary = "팔로우 요청 거절", description = "거절 시 요청자에게 알림이 발송되지 않습니다.")
    public ApiResponse<Void> rejectFollowRequest(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @PathVariable Long requestId
    ) {
        followService.rejectFollowRequest(memberId, requestId);
        return ApiResponse.ok("팔로우 요청을 거절했습니다.", null);
    }

    @DeleteMapping("/{followMemberId}")
    @Operation(summary = "팔로우 끊기")
    public ApiResponse<Void> unfollow(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @PathVariable Long followMemberId
    ) {
        followService.unfollow(memberId, followMemberId);
        return ApiResponse.ok("팔로우를 끊었습니다.", null);
    }

    @GetMapping("/count")
    @Operation(summary = "친구 수 조회", description = "현재 친구 수와 상한을 함께 반환합니다.")
    public ApiResponse<FollowCountResponse> getFollowCount(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.ok("친구 수를 조회했습니다.", followService.getFollowCount(memberId));
    }
}
