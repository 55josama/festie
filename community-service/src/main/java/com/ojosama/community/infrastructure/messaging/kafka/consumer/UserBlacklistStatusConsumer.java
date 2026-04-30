package com.ojosama.community.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.comment.domain.repository.CommentRepository;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import com.ojosama.community.domain.payload.BlacklistStatus;
import com.ojosama.community.domain.payload.UserBlacklistStatusEvent;
import com.ojosama.post.domain.repository.PostRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// 유저 블랙리스트 상태 변경 이벤트 구독.
// 토픽: ${spring.kafka.topic.blacklist-updated} (예: operation.blacklist.updated.v1)
@Slf4j
@Component
@RequiredArgsConstructor
public class UserBlacklistStatusConsumer {

    private static final String CONSUMER_GROUP = "community-service-group";
    private static final String EVENT_TYPE = EventType.USER_BLACKLIST_STATUS_UPDATED.getValue();

    private final ObjectMapper objectMapper;
    private final IdempotentEventHandler idempotentHandler;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @KafkaListener(
            topics = "${spring.kafka.topic.blacklist-updated}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        UserBlacklistStatusEvent event;

        try {
            messageKey = UUID.fromString(record.key());
            event = parse(record.value());
            idempotentHandler.handle(
                    messageKey,
                    CONSUMER_GROUP,
                    record.topic(),
                    EVENT_TYPE,
                    () -> dispatch(event)
            );
            log.info("블랙리스트 상태 이벤트 처리 완료. key={}, userId={}, status={}",
                    record.key(), event.userId(), event.status());
        } catch (RuntimeException e) {
            log.error("블랙리스트 상태 이벤트 처리에 실패했습니다. key={}, payload={}",
                    record.key(), record.value(), e);
            throw e;
        }
    }

    private void dispatch(UserBlacklistStatusEvent event) {
        if (event.userId() == null || event.status() == null) {
            log.warn("블랙리스트 이벤트 필수 필드 누락. event={}. 스킵합니다.", event);
            return;
        }
        if (event.status() == BlacklistStatus.INACTIVE) {
            // 사유 추적 컬럼이 없어 신고 누적 BLOCKED 와 구분 불가 → 운영 합의에 따라 무시
            log.info("INACTIVE 이벤트 수신 — 사유 추적 도입 전이므로 community 측 복구 없이 종료. userId={}",
                    event.userId());
            return;
        }
        // ACTIVE: 해당 유저 콘텐츠 일괄 BLOCKED
        int blockedComments = commentRepository.blockAllByUserId(event.userId());
        //post쪽 일괄 blocked
        log.info("블랙리스트 ACTIVE — 유저 콘텐츠 일괄 BLOCKED. userId={}, comments={}",
                event.userId(), blockedComments);
    }

    private UserBlacklistStatusEvent parse(String payload) {
        try {
            return objectMapper.readValue(payload, UserBlacklistStatusEvent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("블랙리스트 이벤트 페이로드 파싱에 실패했습니다.", e);
        }
    }
}
