package com.ojosama.calendarservice.calendar.infrastructure.persistence;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.model.QCalendar;
import com.ojosama.calendarservice.calendar.domain.repository.CalendarRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CalendarRepositoryImpl implements CalendarRepository {

    private final JpaCalendarRepository jpaCalendarRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public void saveAndFlush(Calendar calendar) {
        jpaCalendarRepository.saveAndFlush(calendar);
    }

    @Override
    public Optional<Calendar> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId) {
        return jpaCalendarRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId);
    }

    @Override
    public List<Calendar> findByUserIdAndYearMonth(UUID userId, int year, int month) {
        QCalendar qCalendar = QCalendar.calendar;

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
    public Optional<Calendar> findByEventScheduleIdAndUserId(UUID scheduleId, UUID userId) {
        return jpaCalendarRepository.findByEventScheduleIdAndUserId(scheduleId, userId);
    }
}
