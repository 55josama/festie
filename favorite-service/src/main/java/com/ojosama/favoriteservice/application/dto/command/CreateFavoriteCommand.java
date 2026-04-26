package com.ojosama.favoriteservice.application.dto.command;

import com.ojosama.favoriteservice.presentation.dto.CreateFavoriteRequestDto;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateFavoriteCommand(
        UUID userId,
        UUID eventId,
        UUID categoryId
) {
    public static CreateFavoriteCommand of(CreateFavoriteRequestDto favoriteDto, UUID userId) {
        return CreateFavoriteCommand.builder()
                .categoryId(favoriteDto.categoryId())
                .eventId(favoriteDto.eventId())
                .userId(userId)
                .build();
    }
}
