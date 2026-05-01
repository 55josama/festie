package com.ojosama.calendarservice.calendar.application;

import com.ojosama.calendarservice.calendar.application.dto.result.CalendarResult;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarErrorCode;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarException;
import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.repository.CalendarRepository;
import com.ojosama.calendarservice.calendar.presentaion.dto.CalendarResponseDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarQueryService {

    private final CalendarRepository calendarRepository;

    public CalendarResponseDto getCalendar(UUID calendarId, UUID userId) {
        Calendar calendar = calendarRepository.findByIdAndUserIdAndDeletedAtIsNull(calendarId, userId)
                .orElseThrow(() -> new CalendarException(CalendarErrorCode.CALENDAR_NOT_FOUND));

        return CalendarResponseDto.from(CalendarResult.from(calendar));
    }

    public List<CalendarResponseDto> getCalendars(UUID userId, int year, int month) {
        List<Calendar> calendars = calendarRepository.findByUserIdAndYearMonth(userId, year, month);
        return calendars.stream()
                .map(calendar -> CalendarResponseDto.from(CalendarResult.from(calendar)))
                .toList();
    }
}
