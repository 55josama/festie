package com.ojosama.notificationservice.application.dto.result;

import java.util.UUID;

public record NotificationToastResult(
        UUID id,
        String title,
        String content
) {
}
