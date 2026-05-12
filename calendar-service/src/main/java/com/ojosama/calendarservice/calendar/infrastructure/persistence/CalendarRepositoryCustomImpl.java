package com.ojosama.calendarservice.calendar.infrastructure.persistence;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.model.EventStatus;
import com.ojosama.calendarservice.calendar.domain.model.QCalendar;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository("calendarRepositoryCustomImpl")
@RequiredArgsConstructor
public class CalendarRepositoryCustomImpl implements CalendarRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;
    private final QCalendar qCalendar = QCalendar.calendar;
    private final static UUID system = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Override
    public List<Calendar> findByUserIdAndYearMonthAndDeletedAtIsNull(UUID userId, int year, int month) {
        // 해당 월 1일
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        // 다음달 1일
        LocalDateTime end = start.plusMonths(1);

        return queryFactory
                .selectFrom(qCalendar)
                .where(
                        qCalendar.userId.eq(userId),
                        qCalendar.eventInfo.eventDate.goe(start), // event_date >= start
                        qCalendar.eventInfo.eventDate.lt(end), // event_date < end
                        qCalendar.deletedAt.isNull()
                ).fetch();

    }

    @Override
    public void deleteAllByEventId(UUID eventId) {
        queryFactory.update(qCalendar)
                .set(qCalendar.deletedAt, LocalDateTime.now())
                .set(qCalendar.deletedBy, system)
                .where(qCalendar.eventInfo.eventId.eq(eventId)
                        .and(qCalendar.deletedAt.isNull()))
                .execute();

        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public void bulkUpdateStatusByEventId(UUID eventId, EventStatus status) {
        queryFactory.update(qCalendar)
                .set(qCalendar.eventInfo.eventStatus, status)
                .where(qCalendar.eventInfo.eventId.eq(eventId)
                        .and(qCalendar.deletedAt.isNull()))
                .execute();

        entityManager.flush();
        entityManager.clear();
    }
}
