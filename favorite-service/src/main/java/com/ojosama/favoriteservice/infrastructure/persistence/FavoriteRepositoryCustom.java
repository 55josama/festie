package com.ojosama.favoriteservice.infrastructure.persistence;

import com.ojosama.favoriteservice.domain.model.EventFieldChange;
import com.ojosama.favoriteservice.domain.model.EventStatus;
import java.util.List;
import java.util.UUID;

public interface FavoriteRepositoryCustom {

    void updateEventInfoBulk(UUID eventId, List<EventFieldChange> changedFields);

    void deleteAllByEventId(UUID eventId);

    void updateStatusAllByEventId(UUID eventId, EventStatus status);
}