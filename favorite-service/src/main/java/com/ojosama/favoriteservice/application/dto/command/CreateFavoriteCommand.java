package com.ojosama.favoriteservice.application.dto.command;

import java.util.UUID;

public record CreateFavoriteCommand(
        UUID eventId,
        UUID categoryId
) {
}
