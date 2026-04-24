package com.ojosama.chatservice.domain.model;

import com.ojosama.chatservice.domain.exception.ChatErrorCode;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.common.audit.BaseEntity;
import com.ojosama.common.exception.CommonErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "p_chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, columnDefinition = "uuid")
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomStatus status;

    @Column(nullable = false)
    private LocalDateTime scheduledOpenAt;

    @Column(nullable = false)
    private LocalDateTime scheduledCloseAt;

    @Column
    private LocalDateTime openedAt;

    @Column
    private LocalDateTime closedAt;

    @Column(columnDefinition = "uuid")
    private UUID forceClosedBy;

    @Builder
    private ChatRoom(UUID eventId, EventCategory category,
                     LocalDateTime scheduledOpenAt, LocalDateTime scheduledCloseAt) {
        if (eventId == null || category == null || scheduledOpenAt == null || scheduledCloseAt == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        if (!scheduledCloseAt.isAfter(scheduledOpenAt)) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_INVALID_TIME);
        }
        this.eventId = eventId;
        this.category = category;
        this.status = ChatRoomStatus.SCHEDULED;
        this.scheduledOpenAt = scheduledOpenAt;
        this.scheduledCloseAt = scheduledCloseAt;
    }

    // 도메인 행위 메서드
    public void open() {
        validateStatus(ChatRoomStatus.SCHEDULED, "채팅방 오픈");
        this.status = ChatRoomStatus.OPEN;
        this.openedAt = LocalDateTime.now();
    }

    public void close() {
        validateStatus(ChatRoomStatus.OPEN, "채팅방 종료");
        this.status = ChatRoomStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    public void forceClose(UUID adminId) {
        if (this.status == ChatRoomStatus.CLOSED || this.status == ChatRoomStatus.FORCE_CLOSED) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_INVALID_TIME);
        }
        this.status = ChatRoomStatus.FORCE_CLOSED;
        this.closedAt = LocalDateTime.now();
        this.forceClosedBy = adminId;
    }

    public boolean isOpen() {
        return this.status == ChatRoomStatus.OPEN;
    }

    // 채팅방 상태 확인 메서드
    private void validateStatus(ChatRoomStatus required, String action) {
        if (this.status != required) {
            throw new IllegalStateException(
                    action + " 불가: 현재 상태 = " + this.status);
        }
    }
}
