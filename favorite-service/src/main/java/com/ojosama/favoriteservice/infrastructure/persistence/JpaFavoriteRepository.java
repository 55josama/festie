package com.ojosama.favoriteservice.infrastructure.persistence;

import com.ojosama.favoriteservice.domain.model.Favorite;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFavoriteRepository extends JpaRepository<Favorite, UUID> {

    Optional<Favorite> findByIdAndUserIdAndDeletedAtIsNull(UUID favoriteId, UUID userId);

    Optional<Favorite> findByEventInfo_EventIdAndUserId(UUID eventId, UUID userId);

    List<Favorite> findByUserIdAndDeletedAtIsNull(UUID userId);

    List<Favorite> findByEventInfo_EventIdAndDeletedAtIsNull(UUID eventId);
}
