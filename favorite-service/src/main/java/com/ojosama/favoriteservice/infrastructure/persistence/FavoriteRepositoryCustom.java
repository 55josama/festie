package com.ojosama.favoriteservice.infrastructure.persistence;

import com.ojosama.favoriteservice.domain.model.EventStatus;
import java.util.UUID;

public interface FavoriteRepositoryCustom {

    void updateEventInfoBulk(UUID eventId, String field, String after);

    void updateEventInfoEventStatusBulk(UUID eventId, EventStatus eventStatus);
}