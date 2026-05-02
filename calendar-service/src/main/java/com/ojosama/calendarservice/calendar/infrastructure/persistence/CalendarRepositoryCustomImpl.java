package com.ojosama.calendarservice.calendar.infrastructure.persistence;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.model.QCalendar;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CalendarRepositoryCustomImpl implements CalendarRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QCalendar qCalendar = QCalendar.calendar;

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
}
