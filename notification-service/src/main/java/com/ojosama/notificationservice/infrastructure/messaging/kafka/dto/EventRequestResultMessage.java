package com.ojosama.notificationservice.infrastructure.messaging.kafka.dto;

import java.util.UUID;

public record EventRequestResultMessage(
        UUID targetId,
        UUID receiverId,
        String resultStatus,
        String eventName
) {
}
