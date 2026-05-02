package com.ojosama.chatservice.domain.model;

import com.ojosama.chatservice.domain.exception.ChatErrorCode;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.common.audit.BaseUserEntity;
import com.ojosama.common.exception.CommonErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "p_chat_room",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_room_event_id", columnNames = "event_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseUserEntity {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, columnDefinition = "uuid")
    private UUID eventId;

    @Column(nullable = false)
    private String eventName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomStatus status;

    @Embedded
    private ChatRoomSchedule schedule;

    @Column
    private LocalDateTime openedAt;

    @Column
    private LocalDateTime closedAt;

    @Column(columnDefinition = "uuid")
    private UUID changedBy;

    @Builder
    private ChatRoom(UUID eventId, String eventName, EventCategory category, ChatRoomSchedule schedule) {
        if (eventId == null || eventName == null || eventName.isBlank() || category == null || schedule == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        this.eventId = eventId;
        this.eventName = eventName;
        this.category = category;
        this.status = ChatRoomStatus.SCHEDULED;
        this.schedule = schedule;
    }

    public void open() {
        validateStatus(ChatRoomStatus.SCHEDULED);
        this.status = ChatRoomStatus.OPEN;
        this.openedAt = LocalDateTime.now();
    }

    public void close() {
        validateStatus(ChatRoomStatus.OPEN);
        this.status = ChatRoomStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    public void forceOpen(UUID adminId) {
        if (adminId == null) {
            throw new ChatException(ChatErrorCode.INVALID_ADMIN_ID);
        }
        if (this.status == ChatRoomStatus.OPEN) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_ALREADY_OPENED);
        }
        this.status = ChatRoomStatus.OPEN;
        this.openedAt = LocalDateTime.now();
        this.changedBy = adminId;
    }

    public void forceClose(UUID adminId) {
        if (adminId == null) {
            throw new ChatException(ChatErrorCode.INVALID_ADMIN_ID);
        }
        if (this.status == ChatRoomStatus.CLOSED) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_ALREADY_ENDED);
        }
        this.status = ChatRoomStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
        this.changedBy = adminId;
    }

    public void reschedule(ChatRoomSchedule schedule) {
        if (schedule == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        this.schedule = schedule;
    }

    // 채팅방 상태 확인 메서드
    private void validateStatus(ChatRoomStatus required) {
        if (this.status != required) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_STATUS_INVALID);
        }
    }

    public boolean isOpen() {
        return this.status == ChatRoomStatus.OPEN;
    }

    public boolean isClosed() {
        return this.status == ChatRoomStatus.CLOSED;
    }

    public boolean shouldOpen(LocalDateTime now) {
        return this.schedule.shouldOpen(now);
    }

    public boolean shouldClose(LocalDateTime now) {
        return this.schedule.shouldClose(now);
    }
}
