package com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import com.ojosama.notificationservice.application.NotificationService;
import com.ojosama.notificationservice.domain.exception.NotificationErrorCode;
import com.ojosama.notificationservice.domain.exception.NotificationException;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.EventRequestCreatedResultMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventRequestResultConsumer {

    private static final String CONSUMER_GROUP = "notification-service-consumer";
    private static final String EVENT_TYPE = EventType.EVENT_REQUEST_RESULT.getValue();

    private final IdempotentEventHandler idempotentEventHandler;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.kafka.topic.event-request-processed}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        EventRequestCreatedResultMessage event;

        try {
            messageKey = UUID.fromString(record.key());
            event = parse(record.value());
            idempotentEventHandler.handle(
                    messageKey,
                    CONSUMER_GROUP,
                    record.topic(),
                    EVENT_TYPE,
                    () -> notificationService.createEventRequestResultNotification(event)
            );
            log.info("Event request created: {}", record.key());
        } catch (RuntimeException e) {
            log.error("요청결과 이벤트 실패 : {}, {}", record.key(), e.getMessage());
            throw e;
        }
    }

    private EventRequestCreatedResultMessage parse(String value) {
        try {
            return objectMapper.readValue(value, EventRequestCreatedResultMessage.class);
        } catch (JsonProcessingException e) {
            throw new NotificationException(NotificationErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
    }
}
