package com.ojosama.calendarservice.calendar.infrastructure.scheduler;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.repository.CalendarRepository;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer.KafkaCalendarSchedulerPublisher;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer.dto.EventImminentMessage;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer.dto.TicketingImminentMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final KafkaCalendarSchedulerPublisher publisher;

    // 행사 스케줄 -> 전날 오후 1시
    //@Scheduled(cron = "0 0 13 * * *")
    // 테스트 -> 1분마다
    @Scheduled(fixedRate = 60000)
    public void eventImminent() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime start = tomorrow.atStartOfDay(); // 내일 00시
        LocalDateTime end = tomorrow.plusDays(1).atStartOfDay(); // 모레 00시

        // 전날 행사 목록
        List<Calendar> calendars = calendarRepository.findByEventInfo_EventDateAndDeletedAtIsNull(start, end);

        Map<UUID, List<Calendar>> groupedCalendars = groupCalendars(calendars);

        groupedCalendars.forEach((eventId, calendarList) -> {
            List<UUID> userIds = calendarList.stream().map(Calendar::getUserId).toList();

            EventImminentMessage event = new EventImminentMessage(
                    eventId,
                    calendarList.getFirst().getEventInfo().getEventName(),
                    calendarList.getFirst().getEventInfo().getEventDate(),
                    userIds
            );
            // 카프카 이벤트 발행
            publisher.publishEventImminent(event);
        });
    }

    // 티켓팅 일정 1시간 마다 (테스트 1초마다)
    @Scheduled(fixedRate = 10000)
    public void ticketingImminent() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        List<Calendar> calendars = calendarRepository.findByEventInfo_EventTicketingDateBetweenAndDeletedAtIsNull(now,
                oneHourLater);

        Map<UUID, List<Calendar>> groupedCalendars = groupCalendars(calendars);

        groupedCalendars.forEach((eventId, calendarList) -> {
            List<UUID> userIds = calendarList.stream().map(Calendar::getUserId).toList();

            TicketingImminentMessage event = new TicketingImminentMessage(
                    eventId,
                    calendarList.getFirst().getEventInfo().getEventName(),
                    calendarList.getFirst().getEventInfo().getEventTicketingDate(),
                    userIds
            );
            // 카프카 이벤트 발행
            publisher.publishTicketingEventImminent(event);
        });

    }

    // eventId 기준 그룹핑
    private Map<UUID, List<Calendar>> groupCalendars(List<Calendar> calendars) {
        return calendars.stream().collect(Collectors.groupingBy(
                c -> c.getEventInfo().getEventId()));
    }
}
