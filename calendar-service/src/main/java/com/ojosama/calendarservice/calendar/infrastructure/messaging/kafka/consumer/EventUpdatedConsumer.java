package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.calendarservice.calendar.application.CalendarService;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarErrorCode;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarException;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.consumer.dto.EventUpdatedMessage;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer.KafkaCalendarPublisher;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer.dto.CalendarEventUpdatedMessage;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventUpdatedConsumer {

    private static final String CONSUMER_GROUP = "calendar-service-group";
    private static final String EVENT_TYPE = EventType.EVENT_UPDATED.getValue();

    private final ObjectMapper objectMapper;
    private final IdempotentEventHandler idempotentEventHandler;
    private final CalendarService calendarService;
    private final KafkaCalendarPublisher publisher;

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
                    () -> {
                        // 유저 정보
                        List<UUID> userIds = calendarService.updateAllByEventId(event.eventId(), event.changedFields());

                        // 컨슈머 DTO -> 프로듀서 DTO 변환
                        List<CalendarEventUpdatedMessage.FieldChange> changedFields = event.changedFields().stream()
                                .map(f -> new CalendarEventUpdatedMessage.FieldChange(f.fieldName(),
                                        f.before().toString(),
                                        f.after().toString()))
                                .toList();

                        publisher.publishCalendarEventUpdated(new CalendarEventUpdatedMessage(event.eventId(),
                                event.eventName(), userIds, changedFields));
                    });
            log.info("행사 수정 이벤트 성공 : {}", record.key());
        } catch (RuntimeException e) {
            log.error("행사 수정 이벤트 실패 : {}, {}", record.key(), e.getMessage());
            throw e;
        }
    }

    private EventUpdatedMessage parse(String payload) {
        log.error("수신 페이로드: {}", payload);
        try {
            return objectMapper.readValue(payload, EventUpdatedMessage.class);
        } catch (JsonProcessingException e) {
            log.error("파싱 에러 : {}", e.getMessage());
            throw new CalendarException(CalendarErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
    }
}
