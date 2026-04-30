package com.ojosama.notificationservice.infrastructure.messaging.kafka.dto;

import java.util.UUID;

public record BlackListSendEmailMessage(
        UUID userId,
        String reason
) {
}
