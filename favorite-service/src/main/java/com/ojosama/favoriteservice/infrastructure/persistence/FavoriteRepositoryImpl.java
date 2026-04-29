package com.ojosama.favoriteservice.infrastructure.persistence;

import com.ojosama.favoriteservice.domain.model.Favorite;
import com.ojosama.favoriteservice.domain.repository.FavoriteRepository;
import java.util.List;
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
    public Optional<Favorite> findByEventInfo_EventIdAndUserId(UUID eventId, UUID userId) {
        return jpaFavoriteRepository.findByEventInfo_EventIdAndUserId(eventId, userId);
    }

    @Override
    public Optional<Favorite> findByIdAndUserIdAndDeletedAtIsNull(UUID favoriteId, UUID userId) {
        return jpaFavoriteRepository.findByIdAndUserIdAndDeletedAtIsNull(favoriteId, userId);
    }

    @Override
    public List<Favorite> findByUserIdAndDeletedAtIsNull(UUID userId) {
        return jpaFavoriteRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    public List<Favorite> findByEventInfo_EventIdAndDeletedAtIsNull(UUID eventId) {
        return jpaFavoriteRepository.findByEventInfo_EventIdAndDeletedAtIsNull(eventId);
    }

}
