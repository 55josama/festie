package com.ojosama.favoriteservice.application.dto.command;

import java.util.UUID;

public record DeleteFavoriteEventCommand(
        UUID eventId
) {
}
