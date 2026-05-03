package com.ojosama.userservice.infrastructure.messaging.kafka.dto;

import java.util.UUID;

public record UserBlacklistStatusEvent(
        UUID userId,
        String status
) {
}