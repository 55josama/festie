package com.ojosama.moderation.infrastructure.messaging.kafka.producer;

import com.ojosama.moderation.domain.event.AiModerationEventProducer;
import com.ojosama.moderation.domain.event.payload.AiEvaluateEvent;
import com.ojosama.moderation.domain.model.entity.AiModeration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiModerationEventProducerImpl implements AiModerationEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.ai-evaluated}")
    private String aiEvaluateTopic;

    @Override
    public void publishEvaluatedEvent(AiModeration moderation) {
        AiEvaluateEvent event = AiEvaluateEvent.from(moderation);

        try {
            kafkaTemplate.send(aiEvaluateTopic, moderation.getTargetId().toString(), event)
                    .get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("AI 모더레이션 평가 완료 이벤트 발행 실패", e);
        }
    }
}
