package com.ojosama.favoriteservice.application.dto.result;

import com.ojosama.favoriteservice.domain.model.Favorite;
import java.util.UUID;
import lombok.Builder;

@Builder
public record FavoriteResult(
        UUID id,
        UUID favoriteId,
        UUID eventId,
        UUID categoryId,
        UUID userId,
        String eventName,
        String eventImg
) {
    public static FavoriteResult from(Favorite favorite) {
        return FavoriteResult.builder()
                .id(favorite.getId())
                .favoriteId(favorite.getId())
                .eventId(favorite.getEventInfo().getEventId())
                .eventName(favorite.getEventInfo().getEventName())
                .categoryId(favorite.getCategoryId())
                .userId(favorite.getUserId())
                .eventImg(favorite.getEventInfo().getEventImg())
                .build();
    }
}
