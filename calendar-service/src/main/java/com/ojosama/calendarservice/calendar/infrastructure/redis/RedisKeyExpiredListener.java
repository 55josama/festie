package com.ojosama.calendarservice.calendar.infrastructure.redis;

import com.ojosama.calendarservice.calendar.domain.event.payload.EventImminentMessage;
import com.ojosama.calendarservice.calendar.domain.event.payload.TicketingImminentMessage;
import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.repository.CalendarRepository;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer.KafkaCalendarSchedulerProducer;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisKeyExpiredListener implements MessageListener {

    private final CalendarRepository calendarRepository;
    private final KafkaCalendarSchedulerProducer publisher;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("만료 된 키 : {}", expiredKey);

        if (expiredKey.startsWith("ticketing-alarm:")) {
            UUID eventId = UUID.fromString(expiredKey.replace("ticketing-alarm:", ""));
            handleTicketingAlarm(eventId);

        } else if (expiredKey.startsWith("event-alarm:")) {
            UUID eventId = UUID.fromString(expiredKey.replace("event-alarm:", ""));
            handleEventAlarm(eventId);
        }
    }

    private void handleTicketingAlarm(UUID eventId) {
        List<Calendar> calendars = calendarRepository
                .findByEventInfo_EventIdAndDeletedAtIsNull(eventId);

        if (calendars.isEmpty()) {
            log.info("티켓팅 알림 대상 캘린더가 없습니다: {}", eventId);
            return;
        }

        List<UUID> userIds = calendars.stream().map(Calendar::getUserId).distinct().toList();
        Calendar first = calendars.get(0);

        publisher.publishTicketingEventImminent(new TicketingImminentMessage(
                eventId,
                first.getEventInfo().getEventName(),
                first.getEventInfo().getEventTicketingDate(),
                userIds
        ));
    }

    private void handleEventAlarm(UUID eventId) {
        List<Calendar> calendars = calendarRepository
                .findByEventInfo_EventIdAndDeletedAtIsNull(eventId);

        if (calendars.isEmpty()) {
            log.info("행사 알림 대상 캘린더가 없습니다: {}", eventId);
            return;
        }

        List<UUID> userIds = calendars.stream().map(Calendar::getUserId).distinct().toList();
        Calendar first = calendars.get(0);

        publisher.publishEventImminent(new EventImminentMessage(
                eventId,
                first.getEventInfo().getEventName(),
                first.getEventInfo().getEventDate(),
                userIds
        ));
    }
}


