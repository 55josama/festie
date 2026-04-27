package com.ojosama.favoriteservice.presentation.dto;

import java.util.UUID;

public record CreateFavoriteResponseDto(
        UUID eventId,
        String eventName,
        String userName
) {
    public static CreateFavoriteResponseDto of(UUID eventId, String eventName, String userName) {
        return new CreateFavoriteResponseDto(eventId, eventName, userName);
    }
}
