package com.ojosama.chatservice.presentation.dto.request;

import com.ojosama.chatservice.domain.model.MessageStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeMessageStatusRequest(
        @NotNull MessageStatus status
) {
}
