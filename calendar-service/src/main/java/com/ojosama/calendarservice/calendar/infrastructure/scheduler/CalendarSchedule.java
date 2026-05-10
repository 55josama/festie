package com.ojosama.calendarservice.calendar.infrastructure.scheduler;

import com.ojosama.calendarservice.calendar.application.CalendarRedisService;
import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.repository.CalendarRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CalendarSchedule {

    private final CalendarRepository calendarRepository;
    private final CalendarRedisService notificationService;

    @Scheduled(cron = "0 0 0 * * *")
    public void registerAlarms() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        // 오늘 티켓팅
        List<Calendar> calendars = calendarRepository.findByEventInfo_EventTicketingDateBetweenAndDeletedAtIsNull(
                today.atStartOfDay(), tomorrow.atStartOfDay());

        calendars.stream()
                .collect(Collectors.toMap(
                        calendar -> calendar.getEventInfo().getEventId(),
                        calendar -> calendar,
                        (a, b) -> a
                ))
                .forEach((eventId, calendar) ->
                        notificationService.registerTicketingAlarm(eventId,
                                calendar.getEventInfo().getEventTicketingDate()));

        log.info("티켓팅 알림 redis 등록 : {}", calendars.size());

        // 내일 행사
        List<Calendar> eventCalendars = calendarRepository
                .findByEventInfo_EventDateAndDeletedAtIsNull(
                        tomorrow.atStartOfDay(),
                        tomorrow.plusDays(1).atStartOfDay()
                );

        eventCalendars.stream()
                .collect(Collectors.toMap(
                        calendar -> calendar.getEventInfo().getEventId(),
                        calendar -> calendar,
                        (a, b) -> a
                ))
                .forEach((eventId, calendar) ->
                        notificationService.registerEventAlarm(eventId,
                                calendar.getEventInfo().getEventDate()));

        log.info("행사 알림 redis 등록 : {}", eventCalendars.size());
    }
}
