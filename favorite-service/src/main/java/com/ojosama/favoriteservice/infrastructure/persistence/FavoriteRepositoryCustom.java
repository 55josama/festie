package com.ojosama.favoriteservice.infrastructure.persistence;

import java.util.UUID;

public interface FavoriteRepositoryCustom {

    void updateEventInfoBulk(UUID eventId, String field, String after);
}