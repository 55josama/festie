package com.ojosama.common.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.comment.domain.model.Comment;
import com.ojosama.comment.domain.model.CommentStatus;
import com.ojosama.comment.domain.repository.CommentRepository;
import com.ojosama.common.domain.payload.TargetBlindedEvent;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import com.ojosama.post.domain.model.Post;
import com.ojosama.post.domain.repository.PostRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

//operation-service->신고 누적 등의 사유로 발행하는 블라인드 이벤트 구독
// 토픽: operation.blacklist.registered.v1
@Slf4j
@Component
@RequiredArgsConstructor
public class TargetBlindedConsumer {

    private static final String CONSUMER_GROUP = "community-service-group";
    private static final String EVENT_TYPE = EventType.TARGET_BLINDED.getValue();

    private final ObjectMapper objectMapper;
    private final IdempotentEventHandler idempotentHandler;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @KafkaListener(
            topics = "${spring.kafka.topic.report-blinded}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        TargetBlindedEvent event;

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
            log.info("블라인드 이벤트 처리 완료. key={}, topic={}, targetType={}, targetId={}",
                    record.key(), record.topic(), event.targetType(), event.targetId());
        } catch (RuntimeException e) {
            log.error("블라인드 이벤트 처리에 실패했습니다. key={}, payload={}",
                    record.key(), record.value(), e);
            throw e;
        }
    }

    private void dispatch(TargetBlindedEvent event) {
        if (event.targetType() == null || event.targetId() == null) {
            log.warn("블라인드 이벤트 필수 필드 누락. event={}. 스킵합니다.", event);
            return;
        }
        switch (event.targetType()) {
            case COMMENT -> blindComment(event.targetId());
            case POST -> blindPost(event.targetId());
            case CHAT -> log.debug("CHAT 타겟으로 스킵. targetId={}", event.targetId());
        }
    }

    private void blindComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            log.warn("블라인드 대상 댓글을 찾을 수 없습니다. commentId={}", commentId);
            return;
        }
        if (comment.getStatus() == CommentStatus.BLOCKED) {
            log.debug("이미 BLOCKED 상태. 추가 처리 없이 종료. commentId={}", commentId);
            return;
        }
        comment.block();
    }

    private void blindPost(UUID postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            log.warn("블라인드 대상 게시글을 찾을 수 없습니다. postId={}", postId);
            return;
        }
        if (post.isBlocked()) {
            log.debug("이미 BLOCKED 상태. 추가 처리 없이 종료. postId={}", postId);
            return;
        }
        post.block();
    }

    private TargetBlindedEvent parse(String payload) {
        try {
            return objectMapper.readValue(payload, TargetBlindedEvent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("블라인드 이벤트 페이로드 파싱에 실패했습니다.", e);
        }
    }
}
