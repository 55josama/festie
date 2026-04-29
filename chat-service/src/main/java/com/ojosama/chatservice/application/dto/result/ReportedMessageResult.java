package com.ojosama.chatservice.application.dto.result;

import com.ojosama.chatservice.domain.model.EventCategory;
import com.ojosama.chatservice.domain.model.Message;
import com.ojosama.chatservice.domain.model.MessageStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReportedMessageResult(
        UUID messageId,
        UUID chatRoomId,
        EventCategory categoryName,
        UUID userId,
        String content,
        MessageStatus status,
        LocalDateTime createdAt
) {
    public static ReportedMessageResult from(Message message, EventCategory categoryName) {
        return new ReportedMessageResult(
                message.getId(),
                message.getChatRoomId(),
                categoryName,
                message.getUserId(),
                message.getContent(),
                message.getStatus(),
                message.getCreatedAt()
        );
    }
}
