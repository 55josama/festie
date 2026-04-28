package com.ojosama.calendarservice.calendar.domain.repository;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalendarRepository {

    void save(Calendar calendar);

    Optional<Calendar> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<Calendar> findByUserIdAndYearMonth(UUID userId, int year, int month);

    Optional<Calendar> findByEventScheduleIdAndUserId(UUID scheduleId, UUID userId);
}
