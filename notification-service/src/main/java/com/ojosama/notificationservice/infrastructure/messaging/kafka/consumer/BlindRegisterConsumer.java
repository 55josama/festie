package com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import com.ojosama.notificationservice.application.NotificationService;
import com.ojosama.notificationservice.application.command.TargetBlindEventCommand;
import com.ojosama.notificationservice.domain.exception.NotificationErrorCode;
import com.ojosama.notificationservice.domain.exception.NotificationException;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.BlindRegisterMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BlindRegisterConsumer {

    private static final String CONSUMER_GROUP = "notification-service-group";
    private static final String EVENT_TYPE = EventType.REPORT_BLINDED.getValue();

    private final IdempotentEventHandler idempotentEventHandler;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.kafka.topic.operation-report-blinded}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        BlindRegisterMessage event;

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
            log.info("Event request created: {}", record.key());
        } catch (RuntimeException e) {
            log.error("블라인드 처리 이벤트 실패 : {}, {}", record.key(), e.getMessage());
            throw e;
        }
    }

    private BlindRegisterMessage parse(String value) {
        try {
            return objectMapper.readValue(value, BlindRegisterMessage.class);
        } catch (JsonProcessingException e) {
            throw new NotificationException(NotificationErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
    }

    private void dispatch(BlindRegisterMessage message) {
        if (message == null) {
            throw new NotificationException(NotificationErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
        notificationService.blindNotification(new TargetBlindEventCommand(message.targetId(), message.targetUserId(),
                message.targetType(), message.category()));
    }
}
