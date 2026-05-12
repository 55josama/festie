package com.ojosama.favoriteservice.infrastructure.persistence;

import com.ojosama.favoriteservice.domain.model.EventFieldChange;
import com.ojosama.favoriteservice.domain.model.EventStatus;
import com.ojosama.favoriteservice.domain.model.QFavorite;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FavoriteRepositoryCustomImpl implements FavoriteRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;
    private final QFavorite qFavorite = QFavorite.favorite;

    private final static UUID system = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Override
    public void updateEventInfoBulk(UUID eventId, List<EventFieldChange> changedFields) {

        var update = jpaQueryFactory.update(qFavorite);
        boolean isChanged = false;

        for (var field : changedFields) {
            if ("name".equals(field.fieldName())) {
                update.set(qFavorite.eventInfo.eventName, field.after());
                isChanged = true;
            } else if ("img".equals(field.fieldName())) {
                update.set(qFavorite.eventInfo.eventImg, field.after());
                isChanged = true;
            }
        }

        if (isChanged) {
            update.set(qFavorite.updatedAt, LocalDateTime.now())
                    .set(qFavorite.updatedBy, system)
                    .where(qFavorite.eventInfo.eventId.eq(eventId)
                            .and(qFavorite.deletedAt.isNull()))
                    .execute();
        } else {
            log.info("수정 필드 없음: {}", eventId);
        }

        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public void deleteAllByEventId(UUID eventId) {
        jpaQueryFactory.update(qFavorite)
                .set(qFavorite.deletedAt, LocalDateTime.now())
                .set(qFavorite.deletedBy, system)
                .where(qFavorite.eventInfo.eventId.eq(eventId)
                        .and(qFavorite.deletedAt.isNull()))
                .execute();

        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public void updateStatusAllByEventId(UUID eventId, EventStatus status) {
        jpaQueryFactory.update(qFavorite)
                .set(qFavorite.updatedAt, LocalDateTime.now())
                .set(qFavorite.updatedBy, system)
                .set(qFavorite.eventInfo.eventStatus, status)
                .where(qFavorite.eventInfo.eventId.eq(eventId)
                        .and(qFavorite.deletedAt.isNull()))
                .execute();

        entityManager.flush();
        entityManager.clear();
    }
}
