package com.ojosama.eventservice.eventrequest.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateEventRequestRequest(
        @NotBlank String title,
        @NotBlank String category,
        @NotBlank
        @Pattern(regexp = "^https?://.+", message = "유효한 URL 형식이어야 합니다.")
        String link,
        String description
) {
}
