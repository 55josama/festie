package com.ojosama.favoriteservice.infrastructure.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import com.ojosama.favoriteservice.application.service.FavoriteService;
import com.ojosama.favoriteservice.domain.exception.FavoriteErrorCode;
import com.ojosama.favoriteservice.domain.exception.FavoriteException;
import com.ojosama.favoriteservice.infrastructure.messaging.kafka.dto.EventDeletedMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventDeletedConsumer {

    private static final String CONSUMER_GROUP = "favorite-service-group";
    private static final String EVENT_TYPE = EventType.EVENT_DELETED.getValue();

    private final ObjectMapper objectMapper;
    private final IdempotentEventHandler idempotentEventHandler;
    private final FavoriteService favoriteService;

    @KafkaListener(
            topics = "${spring.kafka.topic.event-deleted}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        EventDeletedMessage event;

        try {
            messageKey = UUID.fromString(record.key());
            event = parse(record.value());

            if (!messageKey.equals(event.eventId())) {
                log.error("key값과 messageId 불일치 : {}, {}", messageKey, event.eventId());
                throw new FavoriteException(FavoriteErrorCode.INVALID_MESSAGE_PAYLOAD);
            }
            idempotentEventHandler.handle(
                    messageKey,
                    CONSUMER_GROUP,
                    record.topic(),
                    EVENT_TYPE,
                    () -> favoriteService.deleteAllByEventId(event.eventId())
            );
            log.info("Event deleted: {}", record.key());
        } catch (RuntimeException e) {
            log.error("삭제 이벤트 실패 : {}, {}", record.key(), e.getMessage());
            throw e;
        }
    }

    private EventDeletedMessage parse(String payload) {
        try {
            return objectMapper.readValue(payload, EventDeletedMessage.class);
        } catch (JsonProcessingException e) {
            throw new FavoriteException(FavoriteErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
    }
}
