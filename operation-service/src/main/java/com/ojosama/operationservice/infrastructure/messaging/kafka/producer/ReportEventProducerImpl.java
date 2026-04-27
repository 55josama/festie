package com.ojosama.operationservice.infrastructure.messaging.kafka.producer;

import com.ojosama.operationservice.domain.event.ReportEventProducer;
import com.ojosama.operationservice.domain.event.payload.BlacklistRegisterEvent;
import com.ojosama.operationservice.domain.event.payload.TargetBlindEvent;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportEventProducerImpl implements ReportEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.target-blinded}")
    private String targetBlindTopic;

    @Value("${spring.kafka.topic.blacklist-registered}")
    private String blacklistRegisterTopic;

    @Override
    public void publishTargetBlindEvent(TargetBlindEvent event) {
        try {
            kafkaTemplate.send(targetBlindTopic, event.targetId().toString(), event).get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("블라인드 이벤트 발행 실패", e);
        }
    }

    @Override
    public void publishBlacklistRegisterEvent(BlacklistRegisterEvent event) {
        try {
            kafkaTemplate.send(blacklistRegisterTopic, event.targetUserId().toString(), event).get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("블랙리스트 등록 이벤트 발행 실패", e);
        }
    }
}
