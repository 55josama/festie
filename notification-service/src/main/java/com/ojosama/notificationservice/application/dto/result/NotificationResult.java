package com.ojosama.notificationservice.application.dto.result;

import com.ojosama.notificationservice.domain.model.notification.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record NotificationResult(
        UUID id,
        String title,
        String content,
        LocalDateTime readAt
) {
    public static NotificationResult of(Notification notification) {
        return NotificationResult.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .readAt(notification.getReadAt())
                .build();
    }

    public static List<NotificationResult> of(List<Notification> notifications) {
        return notifications.stream()
                .map(NotificationResult::of)
                .toList();
    }
}
