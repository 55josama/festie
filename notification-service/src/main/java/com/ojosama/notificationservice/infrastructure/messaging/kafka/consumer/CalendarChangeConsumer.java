package com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import com.ojosama.notificationservice.application.NotificationService;
import com.ojosama.notificationservice.domain.exception.NotificationErrorCode;
import com.ojosama.notificationservice.domain.exception.NotificationException;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.EventUpdatedMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarChangeConsumer {

    private static final String CONSUMER_GROUP = "notification-service-group";
    private static final String EVENT_TYPE = EventType.CALENDAR_UPDATED.getValue();

    private final IdempotentEventHandler idempotentEventHandler;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.kafka.topic.calendar-updated}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        EventUpdatedMessage event;

        try {
            messageKey = UUID.fromString(record.key());
            event = parse(record.value());

            if (!messageKey.equals(event.eventId())) {
                log.error("key값과 messageId 불일치 : {}, {}", messageKey, event.eventId());
                throw new NotificationException(NotificationErrorCode.INVALID_MESSAGE_PAYLOAD);
            }
            idempotentEventHandler.handle(
                    messageKey,
                    CONSUMER_GROUP,
                    record.topic(),
                    EVENT_TYPE,
                    () -> notificationService.updateEventNotification(event)
            );
            log.info("Event deleted: {}", record.key());
        } catch (RuntimeException e) {
            log.error("수정 이벤트 실패 : {}, {}", record.key(), e.getMessage());
            throw e;
        }
    }

    private EventUpdatedMessage parse(String payload) {
        try {
            return objectMapper.readValue(payload, EventUpdatedMessage.class);
        } catch (JsonProcessingException e) {
            throw new NotificationException(NotificationErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
    }
}
