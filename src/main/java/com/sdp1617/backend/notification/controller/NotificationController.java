package com.sdp1617.backend.notification.controller;

import com.sdp1617.backend.global.common.response.ApiResponse;
import com.sdp1617.backend.notification.dto.NotificationResponse;
import com.sdp1617.backend.notification.dto.PushSettingResponse;
import com.sdp1617.backend.notification.dto.PushSettingUpdateRequest;
import com.sdp1617.backend.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage/notifications")
@Tag(name = "마이페이지 - 알림", description = "푸시 알림 설정, 알림함 조회/읽음 처리 API")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/settings")
    @Operation(summary = "푸시 알림 수신 설정 조회")
    public ApiResponse<PushSettingResponse> getPushSetting(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.ok("푸시 알림 설정을 조회했습니다.", notificationService.getPushSetting(memberId));
    }

    @PatchMapping("/settings")
    @Operation(summary = "푸시 알림 수신 설정 변경")
    public ApiResponse<Void> updatePushSetting(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @RequestBody PushSettingUpdateRequest request
    ) {
        notificationService.updatePushSetting(memberId, request.pushNotificationEnabled());
        return ApiResponse.ok("푸시 알림 설정이 변경되었습니다.", null);
    }

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "최신순으로 알림 목록을 조회합니다.")
    public ApiResponse<List<NotificationResponse>> getNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.ok("알림 목록을 조회했습니다.", notificationService.getNotifications(memberId));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리")
    public ApiResponse<Void> markAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(memberId, notificationId);
        return ApiResponse.ok("알림을 읽음 처리했습니다.", null);
    }
}
