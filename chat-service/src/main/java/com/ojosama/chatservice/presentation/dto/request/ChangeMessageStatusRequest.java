package com.ojosama.chatservice.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChangeMessageStatusRequest(
        @NotNull ChangeMessageStatusActionRequest status
) {
}
