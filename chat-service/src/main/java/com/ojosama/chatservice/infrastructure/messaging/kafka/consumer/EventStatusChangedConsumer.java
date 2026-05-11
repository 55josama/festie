package com.ojosama.chatservice.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.chatservice.application.dto.result.ChatRoomResult;
import com.ojosama.chatservice.application.dto.result.MessageResult;
import com.ojosama.chatservice.application.service.ChatRoomService;
import com.ojosama.chatservice.application.service.MessageService;
import com.ojosama.chatservice.domain.exception.ChatErrorCode;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.chatservice.infrastructure.messaging.kafka.dto.EventStatusChangedEvent;
import com.ojosama.chatservice.presentation.dto.response.MessageWsResponse;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventStatusChangedConsumer {

    private static final String CONSUMER_GROUP = "chat-service-group";
    private static final String EVENT_TYPE = EventType.EVENT_STATUS_CHANGED.getValue();
    private static final String ROOM_TOPIC_PREFIX = "/topic/rooms/";
    private static final String ROOM_TOPIC_SUFFIX = "/messages";
    private static final String CANCELLATION_NOTICE_TEMPLATE = "행사가 취소되어 채팅방이 %s에 닫힙니다";
    private static final DateTimeFormatter NOTICE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MM월 dd일 HH시 mm분");

    private final ObjectMapper objectMapper;
    private final IdempotentEventHandler idempotentHandler;
    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            topics = "${spring.kafka.topic.event-status-changed:event.status.changed.v1}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        try {
            EventStatusChangedEvent event = parse(record.value(), record.topic());
            if (event == null || event.eventId() == null) {
                return;
            }

            UUID messageKey = parseMessageKey(record.key(), event.eventId(), record.topic());
            if (messageKey == null) {
                return;
            }

            idempotentHandler.handle(
                    messageKey,
                    CONSUMER_GROUP,
                    record.topic(),
                    EVENT_TYPE,
                    () -> dispatch(event)
            );
            log.info("행사 상태 변경 메시지 처리를 완료했습니다. key={}, topic={}", record.key(), record.topic());
        } catch (RuntimeException e) {
            log.error("행사 상태 변경 이벤트 처리에 실패했습니다. key={}, topic={}",
                    record.key(), record.topic(), e);
            throw e;
        }
    }

    private void dispatch(EventStatusChangedEvent event) {
        if (!event.isCancelled()) {
            log.info("취소 상태가 아니어서 채팅방 종료시간 조정을 건너뜁니다. eventId={}, afterStatus={}",
                    event.eventId(), event.afterStatus());
            return;
        }

        try {
            // 현재 시간을 취소 시간으로 저장 (정확히는 이벤트 수신 시간)
            LocalDateTime cancelledAt = LocalDateTime.now();
            ChatRoomResult updatedRoom = chatRoomService.changeChatRoomScheduleForCancellation(
                    event.eventId(),
                    cancelledAt
            );

            // 종료 시간 안내 포맷
            LocalDateTime closingAt = updatedRoom.scheduledCloseAt();
            String notice = String.format(
                    CANCELLATION_NOTICE_TEMPLATE,
                    closingAt.format(NOTICE_TIME_FORMATTER)
            );

            // 관리자(시스템) 안내 메시지 자동 출력
            MessageResult message = messageService.createSystemNotice(updatedRoom.chatRoomId(), notice);
            messagingTemplate.convertAndSend(
                    ROOM_TOPIC_PREFIX + updatedRoom.chatRoomId() + ROOM_TOPIC_SUFFIX,
                    MessageWsResponse.from(message)
            );

            log.info("행사 취소를 반영해 채팅방 종료시간을 갱신하고 안내 메시지를 전송했습니다. eventId={}, chatRoomId={}, closeAt={}",
                    event.eventId(), updatedRoom.chatRoomId(), closingAt);
        } catch (ChatException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatus())
                    || ChatErrorCode.CHAT_ROOM_NOT_FOUND.getStatus().equals(e.getStatus())) {
                log.warn("채팅방이 없어 행사 취소 반영을 건너뜁니다. eventId={}", event.eventId());
                return;
            }
            throw e;
        } catch (RuntimeException e) {
            log.error("행사 취소 채팅방 처리 중 오류가 발생했습니다. eventId={}", event.eventId(), e);
            throw e;
        }
    }

    private UUID parseMessageKey(String key, UUID fallbackEventId, String topic) {
        if (key != null && !key.isBlank()) {
            try {
                return UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                log.warn("행사 상태 변경 이벤트 key 형식이 올바르지 않아 payload eventId로 대체합니다. key={}, topic={}",
                        key, topic);
            }
        }
        return fallbackEventId;
    }

    private EventStatusChangedEvent parse(String payload, String topic) {
        if (payload == null || payload.isBlank()) {
            log.warn("행사 상태 변경 이벤트 payload가 비어 있어 건너뜁니다. topic={}", topic);
            return null;
        }
        try {
            return objectMapper.readValue(payload, EventStatusChangedEvent.class);
        } catch (JsonProcessingException e) {
            log.warn("행사 상태 변경 이벤트 페이로드 파싱에 실패해 건너뜁니다. topic={}", topic);
            return null;
        }
    }
}
