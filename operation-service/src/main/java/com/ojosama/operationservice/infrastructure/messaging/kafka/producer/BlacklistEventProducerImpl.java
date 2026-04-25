package com.ojosama.operationservice.infrastructure.messaging.kafka.producer;

import com.ojosama.operationservice.domain.event.BlacklistEventProducer;
import com.ojosama.operationservice.domain.event.payload.UserBlacklistStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlacklistEventProducerImpl implements BlacklistEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.user-blacklist-status}")
    private String statusTopic;

    @Override
    public void publishStatusChangeEvent(UserBlacklistStatusEvent event) {
        kafkaTemplate.send(statusTopic, event);
    }
}
