package com.ojosama.favoriteservice.infrastructure.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import com.ojosama.favoriteservice.application.dto.command.UpdateStatusEventCommand;
import com.ojosama.favoriteservice.application.service.FavoriteService;
import com.ojosama.favoriteservice.domain.exception.FavoriteErrorCode;
import com.ojosama.favoriteservice.domain.exception.FavoriteException;
import com.ojosama.favoriteservice.domain.model.EventStatus;
import com.ojosama.favoriteservice.infrastructure.messaging.kafka.dto.EventStatusUpdatedMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class EventStatusUpdateConsumer {

    private static final String CONSUMER_GROUP = "favorite-service-group";
    private static final String EVENT_TYPE = EventType.EVENT_STATUS_CHANGED.getValue();

    private final ObjectMapper objectMapper;
    private final IdempotentEventHandler idempotentEventHandler;
    private final FavoriteService favoriteService;

    @KafkaListener(
            topics = "${spring.kafka.topic.event-status-changed}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        EventStatusUpdatedMessage event;

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
            log.info("행사 상태 변경 이벤트 성공: {}", record.key());
        } catch (RuntimeException e) {
            log.error("행사 상태 변경 이벤트 실패 : {}, {}", record.key(), e.getMessage());
            throw e;
        }
    }

    private EventStatusUpdatedMessage parse(String payload) {
        try {
            return objectMapper.readValue(payload, EventStatusUpdatedMessage.class);
        } catch (JsonProcessingException e) {
            throw new FavoriteException(FavoriteErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
    }

    private void dispatch(EventStatusUpdatedMessage event) {
        if (event.afterStatus() == null || event.eventId() == null) {
            throw new FavoriteException(FavoriteErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
        favoriteService.updateStatusEventId(
                new UpdateStatusEventCommand(event.eventId(), EventStatus.from(event.afterStatus())));
    }
}
