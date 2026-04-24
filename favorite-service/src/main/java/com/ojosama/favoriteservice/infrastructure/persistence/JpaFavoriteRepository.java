package com.ojosama.favoriteservice.infrastructure.persistence;

import com.ojosama.favoriteservice.domain.model.Favorite;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFavoriteRepository extends JpaRepository<Favorite, UUID> {

    Optional<Favorite> findByIdAndDeletedAtIsNull(UUID favoriteId);

    Boolean existsByUserIdAndEventId(UUID userId, UUID eventId);
}
