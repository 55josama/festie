package com.ojosama.calendarservice.calendar.application;

import com.ojosama.calendarservice.calendar.application.dto.command.CreateCalendarCommand;
import com.ojosama.calendarservice.calendar.application.dto.result.CalendarResult;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarErrorCode;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarException;
import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.model.EventInfo;
import com.ojosama.calendarservice.calendar.domain.repository.CalendarRepository;
import com.ojosama.calendarservice.calendar.presentaion.dto.CalendarResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CalendarCommandService {

    private final CalendarRepository calendarRepository;

    public CalendarResponseDto createCalendar(CreateCalendarCommand command) {

        Optional<Calendar> exists = calendarRepository.findByEventInfo_EventScheduleIdAndUserIdAndDeletedAtIsNull(
                command.eventScheduleId(),
                command.userId());

        if (exists.isPresent()) {
            throw new CalendarException(CalendarErrorCode.EXISTS_CALENDAR);
        }

        // TODO : event-service에서 feign으로 eventScheduleId로 startDate 복사
        LocalDateTime eventStart = LocalDateTime.now();

        // TODO : event-service에서 eventId값으로 ticketing시간, 행사이름 복사
        LocalDateTime ticketingDate = LocalDateTime.now();
        String eventName = "행사";

        Calendar calendar = Calendar.create(command.userId(), command.memo(),
                new EventInfo(command.eventId(), eventName, command.eventScheduleId(), eventStart, ticketingDate));

        calendarRepository.save(calendar);

        return CalendarResponseDto.from(CalendarResult.from(calendar));
    }

    public CalendarResponseDto updateCalendarMemo(UUID calendarId, String memo, UUID userId) {
        Calendar calendar = validateCalendarAlive(calendarId, userId);

        calendar.updateMemo(memo);
        return CalendarResponseDto.from(CalendarResult.from(calendar));
    }

    public void deleteCalendar(UUID calendarId, UUID userId) {
        Calendar calendar = validateCalendarAlive(calendarId, userId);

        calendar.deleted(userId);
    }

    public void deleteAllByEventId(UUID eventId) {
        List<Calendar> calendars = calendarRepository.findAllByEventInfo_EventIdAndDeletedAtIsNull(eventId);
        calendars.forEach(c -> c.deleted(null));
    }

    public void updateEventInfo(UUID eventId, LocalDateTime newDate, String newName, LocalDateTime newTicketingDate) {
        List<Calendar> calendars = calendarRepository.findAllByEventInfo_EventIdAndDeletedAtIsNull(eventId);
        calendars.forEach(c -> c.updateEventInfo(newDate, newName, newTicketingDate));
    }

    private Calendar validateCalendarAlive(UUID calendarId, UUID userId) {
        return calendarRepository.findByIdAndUserIdAndDeletedAtIsNull(calendarId, userId).orElseThrow(() ->
                new CalendarException(CalendarErrorCode.CALENDAR_NOT_FOUND));
    }
}
