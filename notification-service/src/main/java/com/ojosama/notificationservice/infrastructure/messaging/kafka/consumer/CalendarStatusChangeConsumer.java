package com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import com.ojosama.notificationservice.application.NotificationService;
import com.ojosama.notificationservice.application.command.CalendarStatusChangeCommand;
import com.ojosama.notificationservice.domain.exception.NotificationErrorCode;
import com.ojosama.notificationservice.domain.exception.NotificationException;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.CalendarStatusChangeMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarStatusChangeConsumer {
    private static final String CONSUMER_GROUP = "notification-service-group";
    private static final String EVENT_TYPE = EventType.CALENDAR_STATUS_UPDATED.getValue();

    private final ObjectMapper objectMapper;
    private final IdempotentEventHandler idempotentEventHandler;
    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${spring.kafka.topic.calendar-status-changed}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        CalendarStatusChangeMessage event;

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
            log.info("행사 취소 이벤트 성공 : {}", record.key());
        } catch (RuntimeException e) {
            log.error("행사 취소 이벤트 실패 : {}, {}", record.key(), e.getMessage());
            throw e;
        }
    }

    private CalendarStatusChangeMessage parse(String payload) {
        try {
            return objectMapper.readValue(payload, CalendarStatusChangeMessage.class);
        } catch (JsonProcessingException e) {
            throw new NotificationException(NotificationErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
    }

    private void dispatch(CalendarStatusChangeMessage event) {
        if (event == null || event.eventId() == null) {
            throw new NotificationException(NotificationErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
        notificationService.updateEventStatusNotification(
                new CalendarStatusChangeCommand(event.eventId(), event.eventName(), event.status(), event.userIds()));
    }
}
