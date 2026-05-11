package com.ojosama.notificationservice.infrastructure.messaging.kafka.dto;

import java.util.UUID;

public record BlackListRequestMessage(
        UUID targetUserId,
        String reason,
        long blindCount
) {
}
