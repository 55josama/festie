package com.ojosama.notificationservice.infrastructure.messaging.kafka.dto;

import java.util.UUID;

public record BlackListRegisterEventMessage(
        UUID userId,
        String reason
) {
}
