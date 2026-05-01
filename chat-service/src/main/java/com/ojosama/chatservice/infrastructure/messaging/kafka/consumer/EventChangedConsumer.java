package com.ojosama.chatservice.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.chatservice.application.dto.command.ChangeChatRoomScheduleCommand;
import com.ojosama.chatservice.application.service.ChatRoomService;
import com.ojosama.chatservice.infrastructure.messaging.kafka.dto.EventChangedEvent;
import com.ojosama.chatservice.infrastructure.messaging.kafka.dto.EventChangedEvent.FieldChange;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventChangedConsumer {

    private static final String CONSUMER_GROUP = "chat-service-group";
    private static final String EVENT_TYPE = EventType.EVENT_SCHEDULE_CHANGED.getValue();

    private final ObjectMapper objectMapper;
    private final IdempotentEventHandler idempotentHandler;
    private final ChatRoomService chatRoomService;

    @KafkaListener(
            topics = "${spring.kafka.topic.event-changed}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        EventChangedEvent event;

        try {
            messageKey = UUID.fromString(record.key());
            event = parse(record.value());
            idempotentHandler.handle(
                    messageKey,
                    CONSUMER_GROUP,
                    record.topic(),
                    EVENT_TYPE,
                    () -> dispatch(event)
            );
            log.info("행사 일정 변경 메시지 처리를 완료했습니다. key={}, topic={}", record.key(), record.topic());
        } catch (RuntimeException e) {
            log.error("행사 일정 변경 이벤트 처리에 실패했습니다. key={}, payload={}",
                    record.key(), record.value(), e);
            throw e;
        }
    }

    private void dispatch(EventChangedEvent event) {
        if (event == null || event.eventId() == null) {
            throw new IllegalArgumentException("행사 일정 변경 이벤트에 eventId가 없습니다.");
        }
        if (!hasRelevantChanges(event)) {
            log.info("반영할 변경 필드가 없어 채팅방 갱신을 건너뜁니다. eventId={}", event.eventId());
            return;
        }

        LocalDateTime eventStartAt = extractAfter(event, "startAt");
        LocalDateTime eventEndAt = extractAfter(event, "endAt");

        chatRoomService.changeChatRoomSchedule(
                new ChangeChatRoomScheduleCommand(
                        event.eventId(),
                        eventStartAt,
                        eventEndAt
                )
        );
        log.info("행사 변경을 반영해 채팅방 정보를 갱신했습니다. eventId={}", event.eventId());
    }

    private boolean hasRelevantChanges(EventChangedEvent event) {
        return event.changedFields() != null
                && event.changedFields().stream()
                .anyMatch(this::isRelevantField);
    }

    private boolean isRelevantField(FieldChange fieldChange) {
        return fieldChange != null
                && ("startAt".equals(fieldChange.fieldName())
                || "endAt".equals(fieldChange.fieldName()));
    }

    private LocalDateTime extractAfter(EventChangedEvent event, String fieldName) {
        if (event.changedFields() == null) {
            return null;
        }

        return event.changedFields().stream()
                .filter(field -> field != null && fieldName.equals(field.fieldName()))
                .map(FieldChange::after)
                .map(this::toLocalDateTime)
                .findFirst()
                .orElse(null);
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        return LocalDateTime.parse(value.toString());
    }

    private EventChangedEvent parse(String payload) {
        try {
            return objectMapper.readValue(payload, EventChangedEvent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("행사 일정 변경 이벤트 페이로드 파싱에 실패했습니다.", e);
        }
    }
}
