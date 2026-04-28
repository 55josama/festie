package com.ojosama.moderation.infrastructure.messaging.kafka.consumer;

import com.ojosama.moderation.application.service.AiModerationService;
import com.ojosama.moderation.domain.event.payload.AiModerationRequestEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiModerationEventConsumer {
    private final AiModerationService aiModerationService;

    // 채팅용 리스너 (chatBatchFactory 사용)
    @KafkaListener(
            topics = "${spring.kafka.topic.chat-moderation-requested}",
            groupId = "ai-service-group",
            containerFactory = "chatBatchFactory"
    )
    public void consumeChatBatch(List<AiModerationRequestEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        try {
            log.info("[AI 검증] {}개의 검사 요청 배치를 수신했습니다.", events.size());

            aiModerationService.processModerationBatch(events);

        } catch (Exception e) {
            log.error("AI 검증 배치 처리 중 에러가 발생했습니다. 해당 배치를 스킵합니다. 이벤트 개수: {}", events.size(), e);
        }
    }

    // 게시글용 리스너 (communityBatchFactory 사용)
    @KafkaListener(
            topics = "${spring.kafka.topic.community-moderation-requested}",
            groupId = "ai-service-group",
            containerFactory = "communityBatchFactory"
    )
    public void consumeCommunityBatch(List<AiModerationRequestEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        try {
            log.info("[AI 검증] {}개의 검사 요청 배치를 수신했습니다.", events.size());

            aiModerationService.processModerationBatch(events);

        } catch (Exception e) {
            log.error("AI 검증 배치 처리 중 에러가 발생했습니다. 해당 배치를 스킵합니다. 이벤트 개수: {}", events.size(), e);
        }
    }
}
