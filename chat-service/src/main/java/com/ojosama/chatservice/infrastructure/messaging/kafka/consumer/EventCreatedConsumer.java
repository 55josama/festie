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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventCreatedConsumer {

    private final ObjectMapper objectMapper;
    private final ChatRoomService chatRoomService;
    private final ChatRoomSchedulePolicy chatRoomSchedulePolicy;

    @KafkaListener(topics = "${spring.kafka.topic.event-created}", groupId = "chat-service-group")
    public void consume(String payload) {
        EventCreatedEvent event = parse(payload);
        ChatRoomSchedule schedule = chatRoomSchedulePolicy.calculate(event.eventStartAt(), event.eventEndAt());
        EventCategory category = parseCategory(event.categoryCode());

        try {
            chatRoomService.createChatRoom(
                    new CreateChatRoomCommand(
                            event.eventId(),
                            category,
                            schedule.getScheduledOpenAt(),
                            schedule.getScheduledCloseAt()
                    )
            );
            log.info("행사 이벤트로 채팅방을 자동 생성했습니다. eventId={}, categoryCode={}",
                    event.eventId(), event.categoryCode());
        } catch (ChatException e) {
            if (HttpStatus.CONFLICT.equals(e.getStatus())) {
                log.info("채팅방이 이미 존재해서 이벤트를 건너뜁니다. eventId={}", event.eventId());
                return;
            }
            throw e;
        } catch (RuntimeException e) {
            log.error("행사 이벤트로 채팅방 자동 생성에 실패했습니다. eventId={}, payload={}",
                    event.eventId(), payload, e);
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

    private EventCategory parseCategory(String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            throw new IllegalArgumentException("카테고리 코드는 비어 있을 수 없습니다.");
        }
        try {
            return EventCategory.valueOf(categoryCode.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 카테고리 코드입니다: " + categoryCode, e);
        }
    }
}
