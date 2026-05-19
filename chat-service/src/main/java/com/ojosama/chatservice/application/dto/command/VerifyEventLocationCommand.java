package com.ojosama.chatservice.application.dto.command;

import java.math.BigDecimal;
import java.util.UUID;

public record VerifyEventLocationCommand(
        UUID eventId,
        UUID userId,
        BigDecimal currentLatitude,
        BigDecimal currentLongitude
) {
}
