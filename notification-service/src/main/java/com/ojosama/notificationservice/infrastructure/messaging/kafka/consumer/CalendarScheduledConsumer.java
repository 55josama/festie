package com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import com.ojosama.notificationservice.application.NotificationService;
import com.ojosama.notificationservice.domain.exception.NotificationErrorCode;
import com.ojosama.notificationservice.domain.exception.NotificationException;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.CalendarScheduleMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarScheduledConsumer {

    private static final String CONSUMER_GROUP = "notification-service-group";
    private static final String EVENT_TYPE = EventType.SCHEDULE_REMINDER.getValue();

    private final IdempotentEventHandler idempotentEventHandler;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.kafka.topic.calendar-schedule-imminent}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        CalendarScheduleMessage event;

        try {
            messageKey = UUID.fromString(record.key());
            event = parse(record.value());
            idempotentEventHandler.handle(
                    messageKey,
                    CONSUMER_GROUP,
                    record.topic(),
                    EVENT_TYPE,
                    () -> notificationService.createNotificationEvent(event)
            );
            log.info("Event request created: {}", record.key());
        } catch (RuntimeException e) {
            log.error("일정 리마인드 이벤트 실패 : {}, {}", record.key(), e.getMessage());
        }
    }

    private CalendarScheduleMessage parse(String value) {
        try {
            return objectMapper.readValue(value, CalendarScheduleMessage.class);
        } catch (JsonProcessingException e) {
            throw new NotificationException(NotificationErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
    }
}
