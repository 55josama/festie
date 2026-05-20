package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.calendarservice.calendar.application.CalendarService;
import com.ojosama.calendarservice.calendar.domain.event.payload.CalendarEventUpdatedMessage;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarErrorCode;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarException;
import com.ojosama.calendarservice.calendar.domain.model.FieldChange;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.consumer.dto.EventUpdatedMessage;
import com.ojosama.calendarservice.calendar.infrastructure.redis.CalendarRedisService;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
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
public class EventUpdatedConsumer {

    private static final String CONSUMER_GROUP = "calendar-service-group";
    private static final String EVENT_TYPE = EventType.EVENT_UPDATED.getValue();

    private static final Set<String> WATCHED_FIELDS = Set.of(
            "startAt", "ticketingStartAt", "", "location", "eventName"
    );

    private final ObjectMapper objectMapper;
    private final IdempotentEventHandler idempotentEventHandler;
    private final CalendarService calendarService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CalendarRedisService redisService;

    @KafkaListener(
            topics = "${spring.kafka.topic.event-changed}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        EventUpdatedMessage event;

        try {
            messageKey = UUID.fromString(record.key());
            event = parse(record.value());

            idempotentEventHandler.handle(
                    messageKey,
                    CONSUMER_GROUP,
                    record.topic(),
                    EVENT_TYPE,
                    () -> dispatch(event));

            log.info("수정 이벤트 성공: {}", record.key());
        } catch (RuntimeException e) {
            log.error("수정 이벤트 실패 : {}, {}", record.key(), e.getMessage());
            throw e;
        }
    }

    private void dispatch(EventUpdatedMessage event) {

        if (event.eventId() == null || event.changedFields() == null || event.changedFields().isEmpty()) {
            throw new CalendarException(CalendarErrorCode.INVALID_MESSAGE_PAYLOAD);
        }

        // WATCHED_FIELDS 필터링 추가 -> 불필요한 메세지에 관한 건 알림 x
        List<FieldChange> changedFields = event.changedFields().stream()
                .filter(f -> WATCHED_FIELDS.contains(f.fieldName()))
                .map(f -> new FieldChange(
                        f.fieldName(),
                        f.before() != null ? String.valueOf(f.before()) : null,
                        f.after() != null ? String.valueOf(f.after()) : null
                ))
                .toList();

        if (changedFields.isEmpty()) {
            return;
        }

        List<UUID> userIds = calendarService.updateAllByEventId(event.eventId(), changedFields);

        // 당일 변경이면 redis 삭제 -> 일정에 관련된 것
        boolean hasTimeFieldChange = event.changedFields().stream()
                .anyMatch(field -> "ticketingOpenAt".equals(field.fieldName()) || "startAt".equals(field.fieldName()));
        if (hasTimeFieldChange) {
            redisService.deleteAlarms(event.eventId());
        }

        if (event.ticketingOpenAt() != null && event.ticketingOpenAt().toLocalDate().equals(LocalDate.now())) {
            redisService.registerTicketingAlarm(event.eventId(), event.eventName(), event.ticketingOpenAt(), userIds);
        }

        if (event.startAt() != null && event.startAt().toLocalDate().equals(LocalDate.now().plusDays(1))) {
            redisService.registerEventAlarm(event.eventId(), event.eventName(), event.startAt(), userIds);
        }

        // Kafka 발행
        applicationEventPublisher.publishEvent(
                new CalendarEventUpdatedMessage(event.eventId(), event.eventName(), userIds, changedFields));
    }

    private EventUpdatedMessage parse(String payload) {
        try {
            return objectMapper.readValue(payload, EventUpdatedMessage.class);
        } catch (JsonProcessingException e) {
            throw new CalendarException(CalendarErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
    }
}