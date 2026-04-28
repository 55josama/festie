package com.ojosama.chatservice.presentation.dto.request;

import com.ojosama.chatservice.domain.model.EventCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateChatRoomRequest(
        @NotNull UUID eventId,
        @NotBlank String eventName,
        @NotNull EventCategory category,
        @NotNull LocalDateTime scheduledOpenAt,
        @NotNull LocalDateTime scheduledCloseAt
) {
}
