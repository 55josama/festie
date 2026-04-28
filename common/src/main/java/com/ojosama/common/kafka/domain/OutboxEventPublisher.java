package com.ojosama.common.kafka.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

//도메인 서비스에서 outbox 행을 만들 때 쓰는 헬퍼
//이 메서드는 outbox 테이블에 행을 INSERT만 한다. 실제 Kafka 발행은
//OutboxPoller가 별도 트랜잭션으로 수행한다.
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void publish(
            String aggregateType,
            UUID aggregateId,
            EventType eventType,
            String topic,
            Object payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "이벤트 페이로드 직렬화 실패: " + eventType, e);
        }
        OutboxMessage message = OutboxMessage.create(
                aggregateType, aggregateId, eventType, topic, json);
        outboxRepository.save(message);
    }
}
