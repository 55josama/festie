package com.ojosama.chatservice.application.dto.result;

import com.ojosama.chatservice.domain.model.Message;
import com.ojosama.chatservice.domain.model.MessageStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReportedMessageResult(
        UUID messageId,
        UUID chatRoomId,
        String category,
        UUID userId,
        String content,
        MessageStatus status,
        LocalDateTime createdAt
) {
    public static ReportedMessageResult from(Message message, String category) {
        return new ReportedMessageResult(
                message.getId(),
                message.getChatRoomId(),
                category,
                message.getUserId(),
                message.getContent(),
                message.getStatus(),
                message.getCreatedAt()
        );
    }
}
