package com.ojosama.report.infrastructure.messaging.kafka.producer;

import com.ojosama.common.exception.CommonErrorCode;
import com.ojosama.report.domain.event.ReportEventProducer;
import com.ojosama.report.domain.event.payload.BlacklistReviewRequestedEvent;
import com.ojosama.report.domain.event.payload.TargetBlindedEvent;
import com.ojosama.report.domain.exception.ReportException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportEventProducerImpl implements ReportEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.report-blinded}")
    private String targetBlindedTopic;

    @Value("${spring.kafka.topic.blacklist-requested}")
    private String blacklistReviewRequestedTopic;

    @Override
    public void publishTargetBlindEvent(TargetBlindedEvent event) {
        try {
            kafkaTemplate.send(targetBlindedTopic, event.targetId().toString(), event).get(3, TimeUnit.SECONDS);
            log.info("블라인드 처리 이벤트 발행 성공: targetId={}", event.targetId());
        } catch (Exception e) {
            log.error("블라인드 처리 이벤트 발행 실패: targetId={}", event.targetId(), e);
            throw new ReportException(CommonErrorCode.EVENT_PUBLISH_FAILED);
        }
    }

    // 특정 유저의 게시글이 5번 블라인드 처리되는 순간, 시스템이 자동으로 블랙리스트 등록 검토 알림
    @Override
    public void publishBlacklistReviewRequestEvent(BlacklistReviewRequestedEvent event) {
        try {
            kafkaTemplate.send(blacklistReviewRequestedTopic, event.targetUserId().toString(), event).get(3, TimeUnit.SECONDS);
            log.info("블랙리스트 검토 요청 이벤트 발행 성공: userId={}", event.targetUserId());
        } catch (Exception e) {
            log.error("블랙리스트 검토 요청 이벤트 발행 실패: userId={}", event.targetUserId(), e);
            throw new ReportException(CommonErrorCode.EVENT_PUBLISH_FAILED);
        }
    }
}
