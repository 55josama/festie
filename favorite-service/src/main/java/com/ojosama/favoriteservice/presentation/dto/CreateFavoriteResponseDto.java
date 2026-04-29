package com.ojosama.favoriteservice.presentation.dto;

import com.ojosama.favoriteservice.application.dto.result.FavoriteResult;
import java.util.UUID;

public record CreateFavoriteResponseDto(
        UUID eventId,
        String eventName,
        UUID userId
) {
    public static CreateFavoriteResponseDto of(FavoriteResult result) {
        return new CreateFavoriteResponseDto(result.eventId(), result.eventName(), result.userId());
    }
}
