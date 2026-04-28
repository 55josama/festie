package com.ojosama.blacklist.infrastructure.messaging.kafka.producer;

import com.ojosama.common.exception.CommonErrorCode;
import com.ojosama.blacklist.domain.event.BlacklistEventProducer;
import com.ojosama.blacklist.domain.event.payload.BlacklistRegisterEvent;
import com.ojosama.blacklist.domain.event.payload.UserBlacklistStatusEvent;
import com.ojosama.blacklist.domain.exception.BlacklistException;
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

    @Value("${spring.kafka.topic.blacklist-updated}")
    private String updateTopic;

    @Value("${spring.kafka.topic.blacklist-registered}")
    private String blacklistRegisteredTopic;

    @Override
    public void publishStatusChangeEvent(UserBlacklistStatusEvent event) {
        try {
            kafkaTemplate.send(updateTopic, event.userId().toString(), event).get(3, TimeUnit.SECONDS);
            log.info("유저 블랙리스트 상태 변경 이벤트 발행 성공 - topic: {}, userId: {}", updateTopic, event.userId());

        } catch (Exception e) {
            log.error("유저 블랙리스트 상태 변경 이벤트 발행 실패. DB 트랜잭션을 롤백합니다.", e);
            throw new BlacklistException(CommonErrorCode.EVENT_PUBLISH_FAILED);
        }
    }

    // 관리자가 직접 특정 유저를 수동으로 블랙리스트에 등록하는 순간 알림
    @Override
    public void publishBlacklistRegisterEvent(BlacklistRegisterEvent event){
        try {
            kafkaTemplate.send(blacklistRegisteredTopic, event.userId().toString(), event).get(3, TimeUnit.SECONDS);
            log.info("블랙리스트 알림 이벤트 발행 성공: userId={}", event.userId());
        } catch (Exception e) {
            log.error("블랙리스트 알림 이벤트 발행 실패: userId={}", event.userId(), e);
            throw new BlacklistException(CommonErrorCode.EVENT_PUBLISH_FAILED);
        }
    }
}
