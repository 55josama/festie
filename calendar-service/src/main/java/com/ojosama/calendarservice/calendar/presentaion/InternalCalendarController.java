package com.ojosama.calendarservice.calendar.presentaion;


import com.ojosama.calendarservice.calendar.domain.exception.CalendarErrorCode;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarException;
import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.repository.CalendarRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/calendars")
public class InternalCalendarController {

    private final CalendarRepository calendarRepository;

    @GetMapping("/{eventId}")
    public UUID getCalendarUserInfo(@PathVariable UUID eventId) {
        Calendar calendar = calendarRepository.findFirstByEventInfo_EventId(eventId)
                .orElseThrow(() -> new CalendarException(CalendarErrorCode.CALENDAR_NOT_FOUND));

        return calendar.getEventInfo().getEventId();
    }
}
