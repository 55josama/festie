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
    public List<Calendar> findByEventInfo_EventId(UUID eventId) {
        return jpaCalendarRepository.findByEventInfo_EventId(eventId);
    }

    @Override
    public Optional<Calendar> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId) {
        return jpaCalendarRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId);
    }

    @Override
    public List<Calendar> findByUserIdAndYearMonth(UUID userId, int year, int month) {
        return jpaCalendarRepository.findByUserIdAndYearMonth(userId, year, month);
    }

    @Override
    public List<Calendar> findByEventInfo_EventDate(LocalDateTime start, LocalDateTime end) {
        return jpaCalendarRepository.findByEventInfo_EventDateBetween(start, end);
    }

    @Override
    public List<Calendar> findByEventInfo_EventTicketingDateBetween(LocalDateTime now, LocalDateTime oneHourLater) {
        return jpaCalendarRepository.findByEventInfo_EventTicketingDateBetween(now, oneHourLater);
    }

    @Override
    public Optional<Calendar> findByEventInfo_EventIdAndEventInfo_EventDateAndUserIdAndDeletedAtIsNull(
            LocalDateTime eventDate, UUID eventId, UUID userId) {
        return jpaCalendarRepository.findByEventInfo_EventIdAndEventInfo_EventDateAndUserIdAndDeletedAtIsNull(eventId,
                eventDate, userId);
    }
}
