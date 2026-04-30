package com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import com.ojosama.notificationservice.application.NotificationService;
import com.ojosama.notificationservice.domain.exception.NotificationErrorCode;
import com.ojosama.notificationservice.domain.exception.NotificationException;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.TicketingScheduleMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarTicketingConsumer {

    private static final String CONSUMER_GROUP = "notification-service-consumer";
    private static final String EVENT_TYPE = EventType.TICKETING_SCHEDULE_REMINDER.getValue();

    private final IdempotentEventHandler idempotentEventHandler;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.kafka.topic.calendar-ticketing-imminent}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        TicketingScheduleMessage event;

        try {
            messageKey = UUID.fromString(record.key());
            event = parse(record.value());
            idempotentEventHandler.handle(
                    messageKey,
                    CONSUMER_GROUP,
                    record.topic(),
                    EVENT_TYPE,
                    () -> notificationService.createNotificationTicketing(event)
            );
            log.info("Event request created: {}", record.key());
        } catch (RuntimeException e) {
            log.error("티켓팅 리마인드 이벤트 실패 : {}, {}", record.key(), e.getMessage());
            throw e;
        }
    }

    private TicketingScheduleMessage parse(String value) {
        try {
            return objectMapper.readValue(value, TicketingScheduleMessage.class);
        } catch (JsonProcessingException e) {
            throw new NotificationException(NotificationErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
    }
}
