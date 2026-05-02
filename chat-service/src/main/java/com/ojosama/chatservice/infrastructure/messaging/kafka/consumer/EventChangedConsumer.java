package com.ojosama.chatservice.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.chatservice.application.dto.command.ChangeChatRoomScheduleCommand;
import com.ojosama.chatservice.application.service.ChatRoomService;
import com.ojosama.chatservice.domain.exception.ChatErrorCode;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.chatservice.infrastructure.messaging.kafka.dto.EventChangedEvent;
import com.ojosama.chatservice.infrastructure.messaging.kafka.dto.EventChangedEvent.FieldChange;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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
        try {
            UUID messageKey = parseMessageKey(record.key(), record.topic());
            if (messageKey == null) {
                return;
            }

            EventChangedEvent event = parse(record.value(), record.topic());
            if (event == null) {
                return;
            }

            idempotentHandler.handle(
                    messageKey,
                    CONSUMER_GROUP,
                    record.topic(),
                    EVENT_TYPE,
                    () -> dispatch(event)
            );
            log.info("행사 일정 변경 메시지 처리를 완료했습니다. key={}, topic={}", record.key(), record.topic());
        } catch (RuntimeException e) {
            log.error("행사 일정 변경 이벤트 처리에 실패했습니다. key={}, topic={}",
                    record.key(), record.topic(), e);
            throw e;
        }
    }

    private void dispatch(EventChangedEvent event) {
        if (event == null || event.eventId() == null) {
            log.warn("eventId가 없는 행사 일정 변경 이벤트라 건너뜁니다.");
            return;
        }
        if (!hasRelevantChanges(event)) {
            log.info("반영할 변경 필드가 없어 채팅방 갱신을 건너뜁니다. eventId={}", event.eventId());
            return;
        }

        LocalDateTime eventStartAt = extractAfter(event, "startAt");
        LocalDateTime eventEndAt = extractAfter(event, "endAt");
        if (eventStartAt == null && eventEndAt == null) {
            log.info("일정 변경 after 값이 없어 채팅방 갱신을 건너뜁니다. eventId={}", event.eventId());
            return;
        }

        try {
            chatRoomService.changeChatRoomSchedule(
                    new ChangeChatRoomScheduleCommand(
                            event.eventId(),
                            eventStartAt,
                            eventEndAt
                    )
            );
            log.info("행사 변경을 반영해 채팅방 정보를 갱신했습니다. eventId={}", event.eventId());
        } catch (ChatException e) {
            if (e.getStatus().equals(ChatErrorCode.CHAT_ROOM_NOT_FOUND.getStatus())) {
                log.warn("채팅방이 없어 일정 변경을 건너뜁니다. eventId={}", event.eventId());
                return;
            }
            throw e;
        }
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
        try {
            return LocalDateTime.parse(value.toString());
        } catch (DateTimeParseException e) {
            log.warn("일정 시간 형식 파싱에 실패해 해당 필드를 무시합니다. value={}", value);
            return null;
        }
    }

    private UUID parseMessageKey(String key, String topic) {
        if (key == null || key.isBlank()) {
            log.warn("행사 일정 변경 이벤트 key가 비어 있어 건너뜁니다. topic={}", topic);
            return null;
        }
        try {
            return UUID.fromString(key);
        } catch (IllegalArgumentException e) {
            log.warn("행사 일정 변경 이벤트 key 형식이 올바르지 않아 건너뜁니다. key={}, topic={}", key, topic);
            return null;
        }
    }

    private EventChangedEvent parse(String payload, String topic) {
        if (payload == null || payload.isBlank()) {
            log.warn("행사 일정 변경 이벤트 payload가 비어 있어 건너뜁니다. topic={}", topic);
            return null;
        }
        try {
            return objectMapper.readValue(payload, EventChangedEvent.class);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            log.warn("행사 일정 변경 이벤트 페이로드 파싱에 실패해 건너뜁니다. topic={}", topic);
            return null;
        }
    }
}
