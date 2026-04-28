package com.ojosama.notificationservice.presentation.dto;

import com.ojosama.notificationservice.domain.model.notification.Notification;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record NotificationResponse(
        UUID id,
        String title,
        String content,
        LocalDateTime readAt
) {
    public static NotificationResponse of(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .readAt(notification.getReadAt())
                .build();
    }
}
