package com.ojosama.notificationservice.presentation.dto;

import com.ojosama.notificationservice.application.dto.result.NotificationResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record NotificationResponseDto(
        UUID id,
        String title,
        String content,
        LocalDateTime readAt
) {
    public static NotificationResponseDto of(NotificationResult result) {
        return NotificationResponseDto.builder()
                .id(result.id())
                .title(result.title())
                .content(result.content())
                .readAt(result.readAt())
                .build();
    }

    public static List<NotificationResponseDto> of(List<NotificationResult> results) {
        return results.stream()
                .map(NotificationResponseDto::of)
                .toList();
    }
}
