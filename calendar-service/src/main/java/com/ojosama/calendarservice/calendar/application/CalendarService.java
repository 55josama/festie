package com.ojosama.calendarservice.calendar.application;

import com.ojosama.calendarservice.calendar.application.dto.command.CreateCalendarCommand;
import com.ojosama.calendarservice.calendar.application.dto.command.DeleteCalendarCommand;
import com.ojosama.calendarservice.calendar.application.dto.command.UpdateCalendarCommand;
import com.ojosama.calendarservice.calendar.application.dto.command.UpdateStatusEventCommand;
import com.ojosama.calendarservice.calendar.application.dto.query.ListCalendarQuery;
import com.ojosama.calendarservice.calendar.application.dto.result.CalendarResult;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarErrorCode;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarException;
import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.model.EventInfo;
import com.ojosama.calendarservice.calendar.domain.model.EventStatus;
import com.ojosama.calendarservice.calendar.domain.model.FieldChange;
import com.ojosama.calendarservice.calendar.domain.repository.CalendarRepository;
import com.ojosama.calendarservice.calendar.infrastructure.client.EventClient;
import com.ojosama.calendarservice.calendar.infrastructure.client.dto.EventInfoResponseDto;
import com.ojosama.calendarservice.calendar.presentation.dto.response.CalendarResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {

    private final CalendarRepository calendarRepository;
    private final EventClient eventClient;

    public CalendarResponseDto createCalendar(CreateCalendarCommand command) {

        boolean exists = calendarRepository.findByEventInfo_EventIdAndEventInfo_EventDateAndUserIdAndDeletedAtIsNull(
                command.eventId(), command.eventDate(), command.userId()).isPresent();

        if (exists) {
            throw new CalendarException(CalendarErrorCode.EXISTS_CALENDAR);
        }

        EventInfoResponseDto info = eventClient.getEvents(command.eventId());

        Calendar calendar = Calendar.create(command.userId(), command.memo(),
                new EventInfo(command.eventId(), info.name(), command.eventDate(),
                        info.ticketingOpenAt(), EventStatus.valueOf(info.status())));

        calendarRepository.save(calendar);

        log.info("calendar saved");

        return CalendarResponseDto.from(CalendarResult.from(calendar));
    }

    @Transactional(readOnly = true)
    public CalendarResponseDto getCalendar(UUID calendarId, UUID userId) {
        Calendar calendar = calendarRepository.findByIdAndUserIdAndDeletedAtIsNull(calendarId, userId)
                .orElseThrow(() -> new CalendarException(CalendarErrorCode.CALENDAR_NOT_FOUND));

        return CalendarResponseDto.from(CalendarResult.from(calendar));
    }

    @Transactional(readOnly = true)
    public List<CalendarResponseDto> getCalendars(ListCalendarQuery query) {
        List<Calendar> calendars = calendarRepository.findByUserIdAndYearMonthAndDeletedAtIsNull(
                query.userId(), query.year(), query.month());
        return calendars.stream()
                .map(calendar -> CalendarResponseDto.from(CalendarResult.from(calendar)))
                .toList();
    }

    @Transactional
    public CalendarResponseDto updateCalendarMemo(UpdateCalendarCommand command) {
        Calendar calendar = validateCalendarAlive(command.calendarId(), command.userId());
        calendar.updateMemo(command.memo());
        return CalendarResponseDto.from(CalendarResult.from(calendar));
    }

    @Transactional
    public void deleteCalendar(DeleteCalendarCommand command) {
        Calendar calendar = validateCalendarAlive(command.calendarId(), command.userId());
        calendar.deleted(command.userId());
    }

    @Transactional
    public List<UUID> deleteAllByEventId(UUID eventId) {
        List<Calendar> calendarList = validateCalendarAlive(eventId);
        List<UUID> userIds = calendarList.stream().map(Calendar::getUserId).distinct().toList();
        calendarRepository.deleteAllByEventId(eventId);
        return userIds;
    }

    @Transactional
    public List<UUID> updateAllByEventId(UUID eventId, List<FieldChange> changedFields) {
        if (changedFields == null || changedFields.isEmpty()) {
            throw new CalendarException(CalendarErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
        List<Calendar> calendarList = validateCalendarAlive(eventId);
        List<UUID> userIds = calendarList.stream().map(Calendar::getUserId).distinct().toList();

        calendarList.forEach(calendar -> {
            changedFields.forEach(field -> {

                String valueBefore = field.before() != null ? field.before() : null;
                String valueAfter = field.after() != null ? field.after() : null;
                switch (field.fieldName()) {
                    case "startAt" -> {
                        if (valueBefore != null && calendar.getEventInfo().getEventDate()
                                .equals(LocalDateTime.parse(valueBefore))) {
                            calendar.getEventInfo().updateEventDate(LocalDateTime.parse(valueAfter));
                        }
                    }
                    case "ticketingOpenAt" -> {
                        if (field.after() == null) {
                            calendar.getEventInfo().updateTicketingDate(null);
                        } else {
                            calendar.getEventInfo().updateTicketingDate(LocalDateTime.parse(valueAfter));
                        }
                    }
                    case "name" -> calendar.getEventInfo().updateEventName(valueAfter);
                    default -> log.info("필요없는 정보가 넘어왔습니다.");
                }
            });
        });

        return userIds;
    }

    @Transactional
    public List<UUID> bulkUpdateStatusByEventId(UpdateStatusEventCommand command) {
        List<Calendar> calendarList = validateCalendarAlive(command.eventId());
        List<UUID> userIds = calendarList.stream().map(Calendar::getUserId).distinct().toList();
        calendarRepository.bulkUpdateStatusByEventId(command.eventId(), command.status());

        return userIds;
    }

    private Calendar validateCalendarAlive(UUID calendarId, UUID userId) {
        return calendarRepository.findByIdAndUserIdAndDeletedAtIsNull(calendarId, userId)
                .orElseThrow(() -> new CalendarException(CalendarErrorCode.CALENDAR_NOT_FOUND));
    }

    private List<Calendar> validateCalendarAlive(UUID eventId) {
        return calendarRepository.findByEventInfo_EventIdAndDeletedAtIsNull(eventId);
    }
}