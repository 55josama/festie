package com.ojosama.common.kafka.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

//Inbox 패턴 — 컨슈머가 처리한 메시지 키를 기록해 중복 처리를 방지한다.
@Entity
@Table(
        name = "p_inbox_messages",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_inbox_key_group",
                        columnNames = {"message_key", "consumer_group"}
                )
        },
        indexes = {
                @Index(name = "idx_inbox_processed_at", columnList = "processed_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InboxMessage {
    @Id
    private UUID id;

    @Column(name = "message_key", nullable = false)
    private UUID messageKey;

    //delivery-service-group
    @Column(name = "consumer_group", nullable = false, length = 100)
    private String consumerGroup;

    @Column(name = "topic", nullable = false, length = 200)
    private String topic;

    //ORDER_CREATED
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public static InboxMessage of(
            UUID messageKey, String consumerGroup, String topic, String eventType) {
        InboxMessage m = new InboxMessage();
        m.id = UUID.randomUUID();
        m.messageKey = messageKey;
        m.consumerGroup = consumerGroup;
        m.topic = topic;
        m.eventType = eventType;
        m.processedAt = LocalDateTime.now();
        return m;
    }
}
