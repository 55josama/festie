package com.ojosama.eventservice.eventrequest.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record CreateEventRequestRequest(
        @NotBlank String title,
        @NotNull UUID categoryId,
        @NotBlank
        @Pattern(regexp = "^https?://.+", message = "유효한 URL 형식이어야 합니다.")
        String link,
        String description
) {
}
