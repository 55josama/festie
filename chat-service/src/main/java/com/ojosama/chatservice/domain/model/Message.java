package com.ojosama.chatservice.domain.model;

import com.ojosama.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "p_message",
        indexes = @Index(name = "idx_message_chat_room_id", columnList = "chat_room_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID chatRoomId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;

    @Column
    private LocalDateTime blindedAt;

    @Column(columnDefinition = "uuid")
    private UUID blindedBy;

    @Builder
    private Message(UUID chatRoomId, UUID userId, String content) {
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.content = content;
        this.status = MessageStatus.ACTIVE;
    }

    // 도메인 행위 메서드
    public void blind(UUID adminId) {
        this.status = MessageStatus.BLINDED;
        this.blindedAt = LocalDateTime.now();
        this.blindedBy = adminId;
    }

    public void delete() {
        this.status = MessageStatus.DELETED;
    }

    public boolean isVisible() {
        return this.status == MessageStatus.ACTIVE;
    }
}
