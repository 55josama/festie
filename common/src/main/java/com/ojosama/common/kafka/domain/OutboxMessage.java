package com.ojosama.common.kafka.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "p_outbox_messages",
        indexes = {
                @Index(name = "idx_outbox_status_created", columnList = "status, created_at"),
                @Index(name = "idx_outbox_aggregate", columnList = "aggregate_type, aggregate_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxMessage {
    private static final int MAX_RETRY = 5;

    @Id
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "topic", nullable = false, length = 200)
    private String topic;

    /** 멱등 키 — 컨슈머가 inbox에 기록할 때 사용. 발행자 측에서 미리 부여. */
    @Column(name = "message_key", nullable = false)
    private UUID messageKey;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    //에러 로그 기록
    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //발행 시점 기록
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    //낙관적 락 적용
    @Version
    private Long version;

    public static OutboxMessage create(
            String aggregateType,
            UUID aggregateId,
            String eventType,
            String topic,
            String payload) {
        OutboxMessage m = new OutboxMessage();
        m.id = UUID.randomUUID();
        m.aggregateType = Objects.requireNonNull(aggregateType);
        m.aggregateId = Objects.requireNonNull(aggregateId);
        m.eventType = Objects.requireNonNull(eventType);
        m.topic = Objects.requireNonNull(topic);
        m.messageKey = UUID.randomUUID();
        m.payload = Objects.requireNonNull(payload);
        m.status = OutboxStatus.PENDING;
        m.retryCount = 0;
        m.createdAt = LocalDateTime.now();
        return m;
    }

    public void markSent() {
        this.status = OutboxStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markFailed(String error) {
        this.retryCount++;
        this.lastError = error != null && error.length() > 500
                ? error.substring(0, 500) : error;
        if (this.retryCount >= MAX_RETRY) {
            this.status = OutboxStatus.FAILED;
        }
    }
} 
