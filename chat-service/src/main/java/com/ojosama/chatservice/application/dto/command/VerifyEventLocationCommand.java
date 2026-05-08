package com.ojosama.chatservice.application.dto.command;

import java.math.BigDecimal;
import java.util.UUID;

public record VerifyEventLocationCommand(
        UUID eventId,
        BigDecimal currentLatitude,
        BigDecimal currentLongitude
) {
}
