package com.ojosama.favoriteservice.infrastructure.persistence;

import com.ojosama.favoriteservice.domain.model.Favorite;
import com.ojosama.favoriteservice.domain.repository.FavoriteRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class FavoriteRepositoryImpl implements FavoriteRepository {

    private final JpaFavoriteRepository jpaFavoriteRepository;
    private final FavoriteRepositoryCustom favoriteRepositoryCustom;

    public FavoriteRepositoryImpl(
            @Qualifier("jpaFavoriteRepository") JpaFavoriteRepository jpaFavoriteRepository,
            @Qualifier("favoriteRepositoryCustomImpl") FavoriteRepositoryCustom favoriteRepositoryCustom) {
        this.jpaFavoriteRepository = jpaFavoriteRepository;
        this.favoriteRepositoryCustom = favoriteRepositoryCustom;
    }

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
    public Page<Favorite> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable) {
        return jpaFavoriteRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
    }

    @Override
    public List<Favorite> findByEventInfo_EventIdAndDeletedAtIsNull(UUID eventId) {
        return jpaFavoriteRepository.findByEventInfo_EventIdAndDeletedAtIsNull(eventId);
    }

    @Override
    public void updateEventInfoBulk(UUID eventId, String field, String after) {
        favoriteRepositoryCustom.updateEventInfoBulk(eventId, field, after);
    }
}
