package com.ojosama.eventservice.event.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.common.kafka.domain.OutboxMessage;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.model.Event;
import com.ojosama.eventservice.event.domain.repository.EventRepository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledEventActionHandlerImpl {

    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void handle(OutboxMessage message) {
        try {
            LocalDateTime now = LocalDateTime.now();

            // payload 파싱
            Map<String, Object> payload = objectMapper.readValue(
                message.getPayload(), Map.class);

            String eventId = (String) payload.get("eventId");
            String action = (String) payload.get("action");
            String scheduledAtStr = (String) payload.get("scheduledAt");

            LocalDateTime scheduledAt = LocalDateTime.parse(scheduledAtStr);

            // 아직 시간이 안 되었으면 스킵
            if (scheduledAt.isAfter(now)) {
                log.debug("[ScheduledEventAction] 아직 실행 시간이 아님. eventId={}, scheduledAt={}, now={}",
                    eventId, scheduledAt, now);
                return;
            }

            // Event 조회 및 상태 업데이트
            Event event = eventRepository.findById(UUID.fromString(eventId))
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));

            if ("MARK_IN_PROGRESS".equals(action)) {
                event.markInProgress();
                log.info("[ScheduledEventAction] IN_PROGRESS 전환. eventId={}", eventId);
            } else if ("MARK_COMPLETED".equals(action)) {
                event.markCompleted();
                log.info("[ScheduledEventAction] COMPLETED 전환. eventId={}", eventId);
            }

            eventRepository.save(event);

        } catch (Exception e) {
            log.error("[ScheduledEventAction] 처리 실패. messageId={}, error={}",
                message.getId(), e.getMessage(), e);
            message.markFailed("ScheduledEventAction 처리 실패: " + e.getMessage());
        }
    }
}
