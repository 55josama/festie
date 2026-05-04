package com.ojosama.calendarservice.calendar.infrastructure.persistence;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.repository.CalendarRepository;
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
        return jpaCalendarRepository.findByUserIdAndYearMonthAndDeletedAtIsNull(userId, year, month);
    }

    @Override
    public List<Calendar> findByEventInfo_EventDateAndDeletedAtIsNull(LocalDateTime start, LocalDateTime end) {
        return jpaCalendarRepository.findByEventInfo_EventDateBetweenAndDeletedAtIsNull(start, end);
    }

    @Override
    public List<Calendar> findByEventInfo_EventTicketingDateBetweenAndDeletedAtIsNull(LocalDateTime now,
                                                                                      LocalDateTime oneHourLater) {
        return jpaCalendarRepository.findByEventInfo_EventTicketingDateBetweenAndDeletedAtIsNull(now, oneHourLater);
    }

    @Override
    public Optional<Calendar> findByEventInfo_EventIdAndEventInfo_EventDateAndUserIdAndDeletedAtIsNull(
            UUID eventId, LocalDateTime eventDate, UUID userId) {
        return jpaCalendarRepository.findByEventInfo_EventIdAndEventInfo_EventDateAndUserIdAndDeletedAtIsNull(eventId,
                eventDate, userId);
    }

    @Override
    public Optional<Calendar> findFirstByEventInfo_EventId(UUID eventId) {
        return jpaCalendarRepository.findFirstByEventInfo_EventId(eventId);
    }

}
