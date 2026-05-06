package com.ojosama.eventservice.event.infrastructure.persistence;

import com.ojosama.eventservice.event.domain.model.EventScheduleAction;
import com.ojosama.eventservice.event.domain.model.QEventScheduleAction;
import com.ojosama.eventservice.event.domain.model.ScheduleActionStatus;
import com.ojosama.eventservice.event.domain.repository.EventScheduleActionRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventScheduleActionRepositoryImpl implements EventScheduleActionRepository {

    private final JpaEventScheduleActionRepository jpaRepo;
    private final JPAQueryFactory queryFactory;

    @Override
    public EventScheduleAction save(EventScheduleAction action) {
        return jpaRepo.save(action);
    }

    @Override
    public List<EventScheduleAction> findPendingByScheduledAtBefore(LocalDateTime now, Pageable pageable) {
        return queryFactory
            .selectFrom(QEventScheduleAction.eventScheduleAction)
            .where(
                QEventScheduleAction.eventScheduleAction.status.eq(ScheduleActionStatus.PENDING),
                QEventScheduleAction.eventScheduleAction.scheduledAt.loe(now)
            )
            .orderBy(QEventScheduleAction.eventScheduleAction.scheduledAt.asc())
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetch();
    }
}
