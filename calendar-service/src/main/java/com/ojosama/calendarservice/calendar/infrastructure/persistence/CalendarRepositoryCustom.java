package com.ojosama.calendarservice.calendar.infrastructure.persistence;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import java.util.List;
import java.util.UUID;

public interface CalendarRepositoryCustom {

    List<Calendar> findByUserIdAndYearMonth(UUID userId, int year, int month);
}
