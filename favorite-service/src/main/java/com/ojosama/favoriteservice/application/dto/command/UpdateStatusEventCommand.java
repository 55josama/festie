package com.ojosama.favoriteservice.application.dto.command;

import com.ojosama.favoriteservice.domain.model.EventStatus;
import java.util.UUID;

public record UpdateStatusEventCommand(
        UUID eventId,
        EventStatus status
) {
}
