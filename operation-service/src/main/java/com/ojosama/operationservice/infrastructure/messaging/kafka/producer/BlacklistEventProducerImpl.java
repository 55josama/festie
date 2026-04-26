package com.ojosama.operationservice.infrastructure.messaging.kafka.producer;

import com.ojosama.operationservice.domain.event.BlacklistEventProducer;
import com.ojosama.operationservice.domain.event.payload.UserBlacklistStatusEvent;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlacklistEventProducerImpl implements BlacklistEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.user-blacklist-status}")
    private String statusTopic;

    @Override
    public void publishStatusChangeEvent(UserBlacklistStatusEvent event) {
        try {
            // 카프카가 메시지를 정상적으로 받을 때까지 대기
            // 타임아웃(3초) 설정
            kafkaTemplate.send(statusTopic, event.getUserId().toString(), event).get(3, TimeUnit.SECONDS);
            log.info("Kafka 이벤트 발행 성공 - topic: {}, userId: {}", statusTopic, event.getUserId());

        } catch (Exception e) {
            log.error("Kafka 이벤트 발행 실패. DB 트랜잭션을 롤백합니다.", e);
            throw new IllegalStateException("이벤트 발행에 실패했습니다.", e);
        }
    }
}
