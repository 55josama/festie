package com.ojosama.favoriteservice.application.dto.result;

import com.ojosama.favoriteservice.domain.model.Favorite;
import java.util.UUID;

public record CreateFavoriteResult(
        UUID favoriteId,
        UUID eventId,
        UUID categoryId,
        UUID userId,
        String eventName,
        String userName
) {
    public static CreateFavoriteResult of(Favorite favorite, String eventName, String userNam) {
        return new CreateFavoriteResult(
                favorite.getEventId(),
                favorite.getCategoryId(),
                favorite.getUserId(),
                favorite.getId(),
                eventName,
                userNam
        );
    }
}
