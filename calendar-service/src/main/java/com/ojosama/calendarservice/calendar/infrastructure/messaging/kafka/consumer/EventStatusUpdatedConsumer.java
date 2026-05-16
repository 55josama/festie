package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.calendarservice.calendar.application.CalendarService;
import com.ojosama.calendarservice.calendar.application.dto.command.UpdateStatusEventCommand;
import com.ojosama.calendarservice.calendar.domain.event.payload.CalendarEventStatusUpdatedMessage;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarErrorCode;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarException;
import com.ojosama.calendarservice.calendar.domain.model.EventStatus;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.consumer.dto.EventStatusUpdatedMessage;
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
public class EventStatusUpdatedConsumer {

    private static final String CONSUMER_GROUP = "calendar-service-group";
    private static final String EVENT_TYPE = EventType.EVENT_UPDATED.getValue();

    private final ObjectMapper objectMapper;
    private final IdempotentEventHandler idempotentEventHandler;
    private final CalendarService calendarService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CalendarRedisService redisService;

    @KafkaListener(
            topics = "${spring.kafka.topic.event-status-changed}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        EventStatusUpdatedMessage event;

        try {
            messageKey = UUID.fromString(record.key());
            event = parse(record.value());

            idempotentEventHandler.handle(
                    messageKey,
                    CONSUMER_GROUP,
                    record.topic(),
                    EVENT_TYPE,
                    () -> dispatch(event)
            );
            log.info("행사 상태 변경 이벤트 성공: {}", record.key());
        } catch (RuntimeException e) {
            log.error("행사 상태 변경 이벤트 실패 : {}, {}", record.key(), e.getMessage());
            throw e;
        }
    }

    private EventStatusUpdatedMessage parse(String payload) {
        try {
            return objectMapper.readValue(payload, EventStatusUpdatedMessage.class);
        } catch (JsonProcessingException e) {
            throw new CalendarException(CalendarErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
    }

    private void dispatch(EventStatusUpdatedMessage event) {
        if (event.status() == null || event.eventId() == null) {
            throw new CalendarException(CalendarErrorCode.INVALID_MESSAGE_PAYLOAD);
        }

        EventStatus status = EventStatus.from(event.status());

        List<UUID> userIds = calendarService.bulkUpdateStatusByEventId(
                new UpdateStatusEventCommand(event.eventId(), status));

        // 행사 상태 -> 취소
        if (status == EventStatus.CANCELLED) {
            // eventId로 redis 등록되어있는게 있으면 삭제
            redisService.deleteAlarms(event.eventId());

            // 카프카 이벤트 발행
            applicationEventPublisher.publishEvent(
                    new CalendarEventStatusUpdatedMessage(event.eventId(), event.eventName(), event.status(), userIds)
            );
        }
    }
}
