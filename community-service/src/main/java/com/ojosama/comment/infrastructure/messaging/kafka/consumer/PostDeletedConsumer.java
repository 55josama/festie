package com.ojosama.comment.infrastructure.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.comment.domain.repository.CommentRepository;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import com.ojosama.community.domain.event.payload.PostDeletedEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

//PostDeleted 이벤트 컨슈밍.
//토픽: community.post.deleted.v1
@Slf4j
@Component
@RequiredArgsConstructor
public class PostDeletedConsumer {

    private static final String CONSUMER_GROUP = "community-service-group";
    private static final String EVENT_TYPE = EventType.POST_DELETED.getValue();

    private final ObjectMapper objectMapper;
    private final IdempotentEventHandler idempotentHandler;
    private final CommentRepository commentRepository;

    @KafkaListener(
            topics = "${spring.kafka.topic.post-deleted}",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        UUID messageKey;
        PostDeletedEvent event;

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
            log.info("PostDeleted 이벤트 처리 완료. key={}, topic={}, postId={}",
                    record.key(), record.topic(), event.postId());
        } catch (RuntimeException e) {
            log.error("PostDeleted 이벤트 처리에 실패했습니다. key={}, payload={}",
                    record.key(), record.value(), e);
            throw e;
        }
    }

    private void dispatch(PostDeletedEvent event) {
        if (event.postId() == null) {
            log.warn("PostDeleted 이벤트에 postId가 없습니다. event={}. 스킵.", event);
            return;
        }
        LocalDateTime deletedAt = event.deletedAt() != null
                ? event.deletedAt()
                : LocalDateTime.now();
        int affected = commentRepository.softDeleteAllByPostId(event.postId(), deletedAt);
        log.info("PostDeleted cascade — 댓글 소프트 삭제 완료. postId={}, affected={}",
                event.postId(), affected);
    }

    private PostDeletedEvent parse(String payload) {
        try {
            return objectMapper.readValue(payload, PostDeletedEvent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("PostDeleted 이벤트 페이로드 파싱에 실패했습니다.", e);
        }
    }
}
