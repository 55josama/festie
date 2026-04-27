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
public class AiModerationConsumer {
    private final AiModerationService aiModerationService;

    @KafkaListener(topics = "${spring.kafka.topic.ai-moderation-requested}", groupId = "ai-service-group")
    public void consumeModerationRequestBatch(List<AiModerationRequestEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        try {
            log.info("[AI 모더레이션] {}개의 검사 요청 배치를 수신했습니다.", events.size());

            // 서비스 계층으로 배치 전달
            aiModerationService.processModerationBatch(events);

        } catch (Exception e) {
            log.error("AI 모더레이션 배치 처리 중 치명적 에러가 발생했습니다. 해당 배치를 스킵합니다. 이벤트 개수: {}", events.size(), e);
        }
    }
}
