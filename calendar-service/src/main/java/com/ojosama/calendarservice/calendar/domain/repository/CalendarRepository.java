package com.ojosama.calendarservice.calendar.domain.repository;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalendarRepository {

    Optional<Calendar> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<Calendar> findByUserIdAndYearMonth(UUID userId, int year, int month);

    Optional<Calendar> findByEventInfo_EventScheduleIdAndUserId(UUID scheduleId, UUID userId);

    void saveAndFlush(Calendar calendar);
}
