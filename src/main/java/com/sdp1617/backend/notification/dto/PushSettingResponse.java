package com.sdp1617.backend.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PushSettingResponse(
        @Schema(description = "푸시 알림 수신 여부 (가입 시 기본값 true)", example = "true")
        boolean pushNotificationEnabled
) {
}
