package com.ojosama.favoriteservice.infrastructure.persistence;

import com.ojosama.favoriteservice.domain.model.QFavorite;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FavoriteRepositoryCustomImpl implements FavoriteRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;
    private final QFavorite qFavorite = QFavorite.favorite;

    @Override
    public void updateEventInfoBulk(UUID eventId, String field, String after) {

        var update = jpaQueryFactory.update(qFavorite);

        boolean isChanged = false;

        if ("name".equals(field)) {
            update.set(qFavorite.eventInfo.eventName, after);
            isChanged = true;
        } else if ("img".equals(field)) {
            update.set(qFavorite.eventInfo.eventImg, after);
            isChanged = true;
        }

        if (isChanged) {
            update.set(qFavorite.updatedAt, LocalDateTime.now())
                    .set(qFavorite.updatedBy, UUID.fromString("00000000-0000-0000-0000-000000000000"))
                    .where(qFavorite.eventInfo.eventId.eq(eventId))
                    .execute();
        }

        entityManager.flush();
        entityManager.clear();
    }
}
