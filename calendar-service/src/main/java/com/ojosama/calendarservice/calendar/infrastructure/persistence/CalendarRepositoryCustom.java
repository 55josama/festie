package com.ojosama.calendarservice.calendar.infrastructure.persistence;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.model.EventStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CalendarRepositoryCustom {

    List<Calendar> findByUserIdAndYearMonthAndDeletedAtIsNull(UUID userId, int year, int month);

    void deleteAllByEventId(UUID eventId);

    void bulkUpdateStatusByEventId(UUID eventId, EventStatus status);

    List<UUID> updateAllStatusToCancel(UUID eventId, List<LocalDateTime> deletedScheduleIds);
}
