package com.ojosama.chatservice.application.dto.result;

import java.util.UUID;

public record EventLocationVerificationResult(
        UUID eventId,
        boolean nearEvent
) {
}
