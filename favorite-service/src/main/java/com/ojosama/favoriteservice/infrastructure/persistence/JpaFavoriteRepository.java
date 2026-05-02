package com.ojosama.favoriteservice.infrastructure.persistence;

import com.ojosama.favoriteservice.domain.model.Favorite;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFavoriteRepository extends JpaRepository<Favorite, UUID>, FavoriteRepositoryCustom {

    Optional<Favorite> findByIdAndUserIdAndDeletedAtIsNull(UUID favoriteId, UUID userId);

    Optional<Favorite> findByEventInfo_EventIdAndUserId(UUID eventId, UUID userId);

    Page<Favorite> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    List<Favorite> findByEventInfo_EventIdAndDeletedAtIsNull(UUID eventId);
}
