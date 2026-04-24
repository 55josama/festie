package com.ojosama.chatservice.domain.model;

import com.ojosama.chatservice.domain.exception.ChatErrorCode;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.common.exception.CommonErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomSchedule {

    @Column(nullable = false)
    private LocalDateTime scheduledOpenAt;

    @Column(nullable = false)
    private LocalDateTime scheduledCloseAt;

    public ChatRoomSchedule(LocalDateTime scheduledOpenAt, LocalDateTime scheduledCloseAt) {
        if (scheduledOpenAt == null || scheduledCloseAt == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        if (!scheduledCloseAt.isAfter(scheduledOpenAt)) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_INVALID_TIME);
        }
        this.scheduledOpenAt = scheduledOpenAt;
        this.scheduledCloseAt = scheduledCloseAt;
    }

    public boolean shouldOpen(LocalDateTime now) {
        return !now.isBefore(scheduledOpenAt);
    }

    public boolean shouldClose(LocalDateTime now) {
        return !now.isBefore(scheduledCloseAt);
    }
}
