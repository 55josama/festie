package com.ojosama.calendarservice.calendar.infrastructure.persistence;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.repository.CalendarRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository("calendarRepositoryImpl")
public class CalendarRepositoryImpl implements CalendarRepository {

    private final JpaCalendarRepository jpaCalendarRepository;
    private final CalendarRepositoryCustom calendarRepositoryCustom;

    public CalendarRepositoryImpl(
            @Qualifier("jpaCalendarRepository") JpaCalendarRepository jpaCalendarRepository,
            @Qualifier("calendarRepositoryCustomImpl") CalendarRepositoryCustom calendarRepositoryCustom) {
        this.jpaCalendarRepository = jpaCalendarRepository;
        this.calendarRepositoryCustom = calendarRepositoryCustom;
    }

    @Override
    public void save(Calendar calendar) {
        jpaCalendarRepository.save(calendar);
    }

    @Override
    public List<Calendar> findByEventInfo_EventIdAndDeletedAtIsNull(UUID eventId) {
        return jpaCalendarRepository.findByEventInfo_EventIdAndDeletedAtIsNull(eventId);
    }

    @Override
    public Optional<Calendar> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId) {
        return jpaCalendarRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId);
    }

    @Override
    public List<Calendar> findByUserIdAndYearMonthAndDeletedAtIsNull(UUID userId, int year, int month) {
        return calendarRepositoryCustom.findByUserIdAndYearMonthAndDeletedAtIsNull(userId, year, month);
    }

    @Override
    public List<Calendar> findByEventInfo_EventTicketingDateBetweenAndDeletedAtIsNullAndEventStatusTrue(
            LocalDateTime now,
            LocalDateTime oneHourLater) {
        return jpaCalendarRepository.findByEventInfo_EventTicketingDateBetweenAndDeletedAtIsNullAndEventStatusTrue(now,
                oneHourLater);
    }

    @Override
    public Optional<Calendar> findFirstByEventInfo_EventId(UUID eventId) {
        return jpaCalendarRepository.findFirstByEventInfo_EventId(eventId);
    }

    @Override
    public Optional<Calendar> findByEventInfo_EventIdAndEventInfo_EventDateAndUserIdAndDeletedAtIsNull(UUID eventId,
                                                                                                       LocalDateTime eventDate,
                                                                                                       UUID userId) {
        return jpaCalendarRepository.findByEventInfo_EventIdAndEventInfo_EventDateAndUserIdAndDeletedAtIsNull(eventId,
                eventDate, userId);
    }

    @Override
    public List<Calendar> findByEventInfo_EventDateAndDeletedAtIsNullAndEventStatusTrue(LocalDateTime start,
                                                                                        LocalDateTime end) {
        return jpaCalendarRepository.findByEventInfo_EventDateAndDeletedAtIsNullAndEventStatusTrue(start, end);
    }

}
