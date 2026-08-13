package com.sdp1617.backend.notification.dto;

import jakarta.validation.constraints.NotNull;

public record PushSettingUpdateRequest(
        @NotNull(message = "pushNotificationEnabled는 필수입니다.")
        Boolean pushNotificationEnabled
) {
}
