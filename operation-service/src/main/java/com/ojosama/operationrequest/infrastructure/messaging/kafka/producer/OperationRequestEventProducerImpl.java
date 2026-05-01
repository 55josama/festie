package com.ojosama.operationrequest.infrastructure.messaging.kafka.producer;

import com.ojosama.common.exception.CommonErrorCode;
import com.ojosama.operationrequest.domain.event.OperationRequestEventProducer;
import com.ojosama.operationrequest.domain.event.payload.OperationRequestCreatedEvent;
import com.ojosama.operationrequest.domain.exception.OperationRequestException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationRequestEventProducerImpl implements OperationRequestEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.operation-request-created}")
    private String requestCreatedTopic;

    @Override
    public void publishOperationRequestCreateEvent(OperationRequestCreatedEvent event) {
        try {
            kafkaTemplate.send(requestCreatedTopic, event.requestId().toString(), event).get(3, TimeUnit.SECONDS);
            log.info("운영 요청 알림 이벤트 발행 성공: requestId={}", event.requestId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("운영 요청 알림 이벤트 발행 인터럽트: requestId={}", event.requestId(), e);
            throw new OperationRequestException(CommonErrorCode.EVENT_PUBLISH_FAILED);
        } catch (Exception e) {
            log.error("운영 요청 알림 이벤트 발행 실패: requestId={}", event.requestId(), e);
            throw new OperationRequestException(CommonErrorCode.EVENT_PUBLISH_FAILED);
        }
    }
}
