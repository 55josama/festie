package com.ojosama.chatservice.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record VerifyEventLocationRequest(
        @NotNull BigDecimal currentLatitude,
        @NotNull BigDecimal currentLongitude
) {
}
