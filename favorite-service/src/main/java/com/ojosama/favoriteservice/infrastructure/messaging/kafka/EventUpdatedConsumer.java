package com.ojosama.favoriteservice.infrastructure.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import com.ojosama.favoriteservice.application.service.FavoriteService;
import com.ojosama.favoriteservice.domain.exception.FavoriteErrorCode;
import com.ojosama.favoriteservice.domain.exception.FavoriteException;
import com.ojosama.favoriteservice.infrastructure.messaging.kafka.dto.EventUpdatedMessage;
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

    private static final String CONSUMER_GROUP = "favorite-service-group";
    private static final String EVENT_TYPE = EventType.EVENT_SCHEDULE_CHANGED.getValue();

    private final ObjectMapper objectMapper;
    private final IdempotentEventHandler idempotentEventHandler;
    private final FavoriteService favoriteService;

    @KafkaListener(
            topics = "${spring.kafka.topic.event-updated}",
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
                    () -> favoriteService.updateAllByEventId(event.eventId(), event)
            );
            log.info("수정 이벤트 성공: {}", record.key());
        } catch (RuntimeException e) {
            log.error("수정 이벤트 실패 : {}, {}", record.key(), e.getMessage());
            throw e;
        }
    }

    private EventUpdatedMessage parse(String payload) {
        try {
            return objectMapper.readValue(payload, EventUpdatedMessage.class);
        } catch (JsonProcessingException e) {
            throw new FavoriteException(FavoriteErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
    }
}
