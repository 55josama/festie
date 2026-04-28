package com.ojosama.moderation.infrastructure.messaging.kafka.producer;

import com.ojosama.moderation.domain.event.AiModerationEventProducer;
import com.ojosama.moderation.domain.event.payload.AiEvaluateEvent;
import com.ojosama.moderation.domain.exception.AiModerationErrorCode;
import com.ojosama.moderation.domain.exception.AiModerationException;
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiModerationException(AiModerationErrorCode.EVENT_PUBLISH_INTERRUPTED);
        } catch (Exception e) {
            throw new AiModerationException(AiModerationErrorCode.EVENT_PUBLISH_FAILED);
        }
    }
}
