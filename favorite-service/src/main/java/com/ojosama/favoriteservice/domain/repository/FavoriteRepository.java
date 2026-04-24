package com.ojosama.favoriteservice.domain.repository;

import com.ojosama.favoriteservice.domain.model.Favorite;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository {

    Favorite save(Favorite favorite);

    Optional<Favorite> findByIdAndUserIdAndDeletedAtIsNull(UUID favoriteId, UUID userId);
}
