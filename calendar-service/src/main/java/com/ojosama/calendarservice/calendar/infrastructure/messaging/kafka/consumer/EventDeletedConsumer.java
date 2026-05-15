package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.calendarservice.calendar.application.CalendarService;
import com.ojosama.calendarservice.calendar.domain.event.payload.CalendarEventDeletedMessage;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarErrorCode;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarException;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.consumer.dto.EventDeletedMessage;
import com.ojosama.calendarservice.calendar.infrastructure.redis.CalendarRedisService;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventDeletedConsumer {

    private static final String CONSUMER_GROUP = "calendar-service-group";
    private static final String EVENT_TYPE = EventType.EVENT_DELETED.getValue();

    private final ObjectMapper objectMapper;
    private final IdempotentEventHandler idempotentEventHandler;
    private final CalendarService calendarService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CalendarRedisService redisService;

    @KafkaListener(
            topics = "${spring.kafka.topic.event-deleted}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        EventDeletedMessage event;

        try {
            messageKey = UUID.fromString(record.key());
            event = parse(record.value());

            idempotentEventHandler.handle(
                    messageKey,
                    CONSUMER_GROUP,
                    record.topic(),
                    EVENT_TYPE,
                    () -> dispatch(event));
            log.info("삭제 이밴트 성공: {}", record.key());
        } catch (RuntimeException e) {
            log.error("삭제 이벤트 실패 : {}, {}", record.key(), e.getMessage());
            throw e;
        }
    }

    private EventDeletedMessage parse(String payload) {
        try {
            return objectMapper.readValue(payload, EventDeletedMessage.class);
        } catch (JsonProcessingException e) {
            throw new CalendarException(CalendarErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
    }

    private void dispatch(EventDeletedMessage event) {
        List<UUID> userIds = calendarService.deleteAllByEventId(event.eventId());
        redisService.deleteAlarms(event.eventId());
        // 알림으로 kafka 전송
        applicationEventPublisher.publishEvent(
                new CalendarEventDeletedMessage(event.eventId(), event.eventName(), userIds));
    }
}
