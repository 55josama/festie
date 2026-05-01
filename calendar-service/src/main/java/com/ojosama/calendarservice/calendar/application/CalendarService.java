package com.ojosama.calendarservice.calendar.application;

import com.ojosama.calendarservice.calendar.application.dto.command.CreateCalendarCommand;
import com.ojosama.calendarservice.calendar.application.dto.result.CalendarResult;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarErrorCode;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarException;
import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.model.EventInfo;
import com.ojosama.calendarservice.calendar.domain.repository.CalendarRepository;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.consumer.dto.EventUpdatedMessage.FieldChange;
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
public class CalendarService {

    private final CalendarRepository calendarRepository;

    public CalendarResponseDto createCalendar(CreateCalendarCommand command) {

        Optional<Calendar> exists = calendarRepository.findByEventInfo_EventIdAndEventInfo_EventDateAndUserIdAndDeletedAtIsNull(
                command.eventId(),
                command.eventDate(),
                command.userId());

        if (exists.isPresent()) {
            throw new CalendarException(CalendarErrorCode.EXISTS_CALENDAR);
        }

        Calendar calendar = Calendar.create(command.userId(), command.memo(),
                new EventInfo(command.eventId(), command.eventName(), command.eventDate(), command.ticketingDate()));

        calendarRepository.save(calendar);

        return CalendarResponseDto.from(CalendarResult.from(calendar));
    }

    @Transactional(readOnly = true)
    public CalendarResponseDto getCalendar(UUID calendarId, UUID userId) {
        Calendar calendar = validateCalendarAlive(calendarId, userId);

        return CalendarResponseDto.from(CalendarResult.from(calendar));
    }

    @Transactional(readOnly = true)
    public List<CalendarResponseDto> getCalendars(UUID userId, int year, int month) {
        List<Calendar> calendars = calendarRepository.findByUserIdAndYearMonthAndDeletedAtIsNull(userId, year, month);
        return calendars.stream()
                .map(calendar -> CalendarResponseDto.from(CalendarResult.from(calendar)))
                .toList();
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

    // 행사 삭제로 인한 캘린더 일정 삭제
    public List<UUID> deleteAllByEventId(UUID eventID) {
        List<Calendar> calendarList = validateCalendarAlive(eventID);

        List<UUID> userIds = calendarList.stream().map(Calendar::getUserId).toList();

        calendarList.forEach(calendar -> calendar.deleted(UUID.fromString("00000000-0000-0000-0000-000000000000")));

        return userIds;
    }

    // 행사 변경(일정, 이름, 시작시간, 티켓팅시간)으로 인한 캘린더 일정 변경
    public List<UUID> updateAllByEventId(UUID eventId, List<FieldChange> changedFields) {
        if (changedFields == null || changedFields.isEmpty()) {
            throw new CalendarException(CalendarErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
        List<Calendar> calendarList = validateCalendarAlive(eventId);

        List<UUID> userIds = calendarList.stream().map(Calendar::getUserId).toList();

        // 행사에서 변경 된 필드를 가지고 수정
        calendarList.forEach(calendar -> {
            changedFields.forEach(field -> {
                switch (field.fieldName()) {
                    case "eventDate" -> calendar.getEventInfo().updateEventDate(LocalDateTime.parse(field.after()));
                    case "ticketingDate" -> {
                        if (field.after() == null || field.after().isBlank()) {
                            calendar.getEventInfo().updateTicketingDate(null);
                        } else {
                            calendar.getEventInfo().updateTicketingDate(LocalDateTime.parse(field.after()));
                        }
                    }
                    case "eventName" -> calendar.getEventInfo().updateEventName(field.after());
                }
            });
        });

        return userIds;
    }


    private Calendar validateCalendarAlive(UUID calendarId, UUID userId) {

        return calendarRepository.findByIdAndUserIdAndDeletedAtIsNull(calendarId, userId).orElseThrow(() ->
                new CalendarException(CalendarErrorCode.CALENDAR_NOT_FOUND));
    }

    private List<Calendar> validateCalendarAlive(UUID eventId) {
        return calendarRepository.findByEventInfo_EventIdAndDeletedAtIsNull(eventId);
    }

}
