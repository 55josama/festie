package com.ojosama.chatservice.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.chatservice.application.dto.command.CreateChatRoomCommand;
import com.ojosama.chatservice.application.service.ChatRoomSchedulePolicy;
import com.ojosama.chatservice.application.service.ChatRoomService;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.chatservice.domain.model.ChatRoomSchedule;
import com.ojosama.chatservice.domain.model.EventCategory;
import com.ojosama.chatservice.infrastructure.messaging.kafka.dto.EventCreatedEvent;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventCreatedConsumer {

    private static final String CONSUMER_GROUP = "chat-service-group";
    private static final String EVENT_TYPE = EventType.EVENT_CREATED.getValue();

    private final ObjectMapper objectMapper;
    private final IdempotentEventHandler idempotentHandler;
    private final ChatRoomService chatRoomService;
    private final ChatRoomSchedulePolicy chatRoomSchedulePolicy;

    @KafkaListener(
            topics = "${spring.kafka.topic.event-created}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        EventCreatedEvent event;

        try {
            messageKey = UUID.fromString(record.key());
            event = parse(record.value());
            idempotentHandler.handle(
                    messageKey,
                    CONSUMER_GROUP,
                    record.topic(),
                    EVENT_TYPE,
                    () -> dispatch(event)
            );
            log.info("행사 생성 메시지 처리를 완료했습니다. key={}, topic={}", record.key(), record.topic());
        } catch (RuntimeException e) {
            log.error("행사 이벤트로 채팅방 자동 생성에 실패했습니다. key={}, payload={}",
                    record.key(), record.value(), e);
            throw e;
        }
    }

    private void dispatch(EventCreatedEvent event) {
        try {
            ChatRoomSchedule schedule = chatRoomSchedulePolicy.calculate(
                    event.eventStartAt(),
                    event.eventEndAt()
            );
            EventCategory category = parseCategory(event.categoryName());

            chatRoomService.createChatRoom(
                    new CreateChatRoomCommand(
                            event.eventId(),
                            event.eventName(),
                            category,
                            schedule.getScheduledOpenAt(),
                            schedule.getScheduledCloseAt()
                    )
            );
            log.info("행사 이벤트로 채팅방을 자동 생성했습니다. eventId={}, eventName={}, categoryName={}",
                    event.eventId(), event.eventName(), event.categoryName());
        } catch (ChatException e) {
            if (HttpStatus.CONFLICT.equals(e.getStatus())) {
                log.info("채팅방이 이미 존재해서 이벤트를 건너뜁니다. eventId={}", event.eventId());
                return;
            }
            throw e;
        }
    }

    private EventCreatedEvent parse(String payload) {
        try {
            return objectMapper.readValue(payload, EventCreatedEvent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("행사 생성 이벤트 페이로드 파싱에 실패했습니다.", e);
        }
    }

    private EventCategory parseCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException("카테고리 이름은 비어 있을 수 없습니다.");
        }
        try {
            return EventCategory.valueOf(categoryName.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 카테고리 이름입니다: " + categoryName, e);
        }
    }
}
