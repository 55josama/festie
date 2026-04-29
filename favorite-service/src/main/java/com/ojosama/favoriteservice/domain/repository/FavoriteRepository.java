package com.ojosama.favoriteservice.domain.repository;

import com.ojosama.favoriteservice.domain.model.Favorite;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository {

    Favorite save(Favorite favorite);

    Optional<Favorite> findByEventInfo_EventIdAndUserId(UUID eventId, UUID userId);

    Optional<Favorite> findByIdAndUserIdAndDeletedAtIsNull(UUID favoriteId, UUID userId);

    List<Favorite> findByUserIdAndDeletedAtIsNull(UUID userId);

    List<Favorite> findByEventInfo_EventIdAndDeletedAtIsNull(UUID eventId);
}
