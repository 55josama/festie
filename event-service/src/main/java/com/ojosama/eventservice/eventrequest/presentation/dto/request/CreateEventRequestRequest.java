package com.ojosama.eventservice.eventrequest.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateEventRequestRequest(
        @NotBlank String title,
        @NotBlank String category,
        @NotBlank String link,
        String description
) {
}
