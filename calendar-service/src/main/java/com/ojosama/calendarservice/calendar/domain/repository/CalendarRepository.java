package com.ojosama.calendarservice.calendar.domain.repository;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalendarRepository {

    Optional<Calendar> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<Calendar> findByUserIdAndYearMonthAndDeletedAtIsNull(UUID userId, int year, int month);

    void save(Calendar calendar);

    List<Calendar> findByEventInfo_EventIdAndDeletedAtIsNull(UUID eventId);

    List<Calendar> findByEventInfo_EventDateAndDeletedAtIsNull(LocalDateTime start, LocalDateTime end);

    List<Calendar> findByEventInfo_EventTicketingDateBetweenAndDeletedAtIsNull(LocalDateTime now,
                                                                               LocalDateTime oneHourLater);

    Optional<Calendar> findByEventInfo_EventIdAndEventInfo_EventDateAndUserIdAndDeletedAtIsNull(
            UUID eventId, LocalDateTime localDateTime, UUID userId);
}
