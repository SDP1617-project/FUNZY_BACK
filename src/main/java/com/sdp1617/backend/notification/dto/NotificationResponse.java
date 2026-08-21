package com.sdp1617.backend.notification.dto;

import com.sdp1617.backend.notification.entity.Notification;
import com.sdp1617.backend.notification.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record NotificationResponse(
        @Schema(description = "알림 ID", example = "1")
        Long id,

        @Schema(description = "알림 유형", example = "LETTER")
        NotificationType type,

        @Schema(description = "알림 내용", example = "새 편지가 도착했습니다.")
        String content,

        @Schema(description = "읽음 여부", example = "false")
        boolean read,

        @Schema(description = "알림 생성 시각")
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getContent(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
