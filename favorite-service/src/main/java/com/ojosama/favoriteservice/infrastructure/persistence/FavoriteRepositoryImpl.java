package com.ojosama.favoriteservice.infrastructure.persistence;

import com.ojosama.favoriteservice.domain.model.Favorite;
import com.ojosama.favoriteservice.domain.repository.FavoriteRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FavoriteRepositoryImpl implements FavoriteRepository {

    private final JpaFavoriteRepository jpaFavoriteRepository;

    @Override
    public Favorite save(Favorite favorite) {
        return jpaFavoriteRepository.save(favorite);
    }

    @Override
    public Optional<Favorite> findByIdAndDeletedAtIsNull(UUID favoriteId) {
        return jpaFavoriteRepository.findByIdAndDeletedAtIsNull(favoriteId);
    }

    @Override
    public Boolean existsByUserIdAndEventId(UUID userId, UUID eventId) {
        return jpaFavoriteRepository.existsByUserIdAndEventId(userId, eventId);
    }

}
