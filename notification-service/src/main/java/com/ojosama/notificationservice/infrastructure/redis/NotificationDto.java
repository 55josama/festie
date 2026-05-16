package com.ojosama.notificationservice.infrastructure.redis;

import java.util.UUID;

public record NotificationDto(
        UUID userId,
        Object data
) {
}