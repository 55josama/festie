package com.ojosama.favoriteservice.domain.repository;

import com.ojosama.favoriteservice.domain.model.Favorite;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteRepository {

    Favorite save(Favorite favorite);

    Optional<Favorite> findByEventInfo_EventIdAndUserId(UUID eventId, UUID userId);

    Optional<Favorite> findByIdAndUserIdAndDeletedAtIsNull(UUID favoriteId, UUID userId);

    Page<Favorite> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    List<Favorite> findByEventInfo_EventIdAndDeletedAtIsNull(UUID eventId);

    void updateEventInfoBulk(UUID eventId, String field, String after);
}
