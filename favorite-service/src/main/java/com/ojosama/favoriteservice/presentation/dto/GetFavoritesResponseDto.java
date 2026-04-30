package com.ojosama.favoriteservice.presentation.dto;

import com.ojosama.favoriteservice.application.dto.result.FavoriteResult;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GetFavoritesResponseDto(
        UUID id,
        UUID favoriteId,
        UUID eventId,
        UUID categoryId,
        UUID userId,
        String eventName,
        String eventImg
) {
    public static GetFavoritesResponseDto from(FavoriteResult result) {
        return GetFavoritesResponseDto.builder()
                .id(result.id())
                .eventName(result.eventName())
                .favoriteId(result.favoriteId())
                .eventId(result.eventId())
                .categoryId(result.categoryId())
                .userId(result.userId())
                .eventImg(result.eventImg())
                .build();


    }
}
