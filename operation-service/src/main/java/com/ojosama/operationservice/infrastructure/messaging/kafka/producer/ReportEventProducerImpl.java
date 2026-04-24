package com.ojosama.operationservice.infrastructure.messaging.kafka.producer;

import com.ojosama.operationservice.domain.event.ReportEventProducer;
import com.ojosama.operationservice.domain.event.payload.BlacklistRegisterEvent;
import com.ojosama.operationservice.domain.event.payload.TargetBlindEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportEventProducerImpl implements ReportEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.target-blind}")
    private String targetBlindTopic;

    @Value("${spring.kafka.topic.blacklist-register}")
    private String blacklistRegisterTopic;

    @Override
    public void publishTargetBlindEvent(TargetBlindEvent event) {
        kafkaTemplate.send(targetBlindTopic, event);
    }

    @Override
    public void publishBlacklistRegisterEvent(BlacklistRegisterEvent event) {
        kafkaTemplate.send(blacklistRegisterTopic, event);
    }
}
