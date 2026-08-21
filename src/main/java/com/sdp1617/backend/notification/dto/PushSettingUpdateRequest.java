package com.sdp1617.backend.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record PushSettingUpdateRequest(
        @Schema(description = "변경할 푸시 알림 수신 여부", example = "false")
        @NotNull(message = "pushNotificationEnabled는 필수입니다.")
        Boolean pushNotificationEnabled
) {
}
