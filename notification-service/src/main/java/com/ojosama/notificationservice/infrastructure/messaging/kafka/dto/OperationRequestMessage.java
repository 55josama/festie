package com.ojosama.notificationservice.infrastructure.messaging.kafka.dto;

import java.util.UUID;

public record OperationRequestMessage(
        UUID requestId,
        UUID requesterId,
        String title
) {
}
