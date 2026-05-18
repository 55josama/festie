package com.ojosama.calendarservice.calendar.infrastructure.scheduler;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.repository.CalendarRepository;
import com.ojosama.calendarservice.calendar.infrastructure.redis.CalendarRedisService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
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
                .collect(Collectors.groupingBy(
                        calendar -> calendar.getEventInfo().getEventId()
                ))
                .forEach((eventId, calendarList) -> {
                    List<UUID> userIds = calendarList.stream()
                            .map(Calendar::getUserId)
                            .distinct()
                            .toList();
                    String eventName = calendarList.get(0).getEventInfo().getEventName();
                    LocalDateTime ticketingDate = calendarList.get(0).getEventInfo().getEventTicketingDate();
                    // 일정 알림 redis 등록
                    notificationService.registerTicketingAlarm(eventId, eventName, ticketingDate, userIds);
                });

        log.info("티켓팅 알림 redis 등록 : {}", calendars.size());

        // 내일 행사
        List<Calendar> eventCalendars = calendarRepository
                .findByEventInfo_EventDateBetweenAndDeletedAtIsNull(
                        tomorrow.atStartOfDay(),
                        tomorrow.atTime(LocalTime.MAX)
                );

        eventCalendars.stream()
                .collect(Collectors.groupingBy(calendar -> calendar.getEventInfo().getEventId()))
                .forEach((eventId, calendarList) -> {
                    List<UUID> userIds = calendarList.stream()
                            .map(Calendar::getUserId)
                            .distinct()
                            .toList();
                    String eventName = calendarList.get(0).getEventInfo().getEventName();
                    // 일정 알림 redis 등록
                    notificationService.registerEventAlarm(eventId, eventName,
                            calendarList.get(0).getEventInfo().getEventDate(), userIds);
                });

        log.info("행사 알림 redis 등록 : {}", eventCalendars.size());

        if (calendars.isEmpty() && eventCalendars.isEmpty()) {
            log.info("오늘 처리할 알림 예약 대상이 없습니다.");
        }
    }
}
