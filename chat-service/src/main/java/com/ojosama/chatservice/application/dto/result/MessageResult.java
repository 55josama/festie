package com.ojosama.chatservice.application.dto.result;

import com.ojosama.chatservice.domain.model.Message;
import com.ojosama.chatservice.domain.model.MessageType;
import com.ojosama.chatservice.domain.model.MessageStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResult(
        UUID messageId,
        UUID chatRoomId,
        UUID userId,
        String writerNickname,
        MessageType messageType,
        String content,
        MessageStatus status,
        LocalDateTime createdAt
) {
    private static final UUID SYSTEM_NOTICE_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static MessageResult from(Message message) {
        return new MessageResult(
                message.getId(),
                message.getChatRoomId(),
                message.getUserId(),
                message.getWriterNickname(),
                resolveMessageType(message),
                message.getContent(),
                message.getStatus(),
                message.getCreatedAt()
        );
    }

    private static MessageType resolveMessageType(Message message) {
        if (message != null && SYSTEM_NOTICE_USER_ID.equals(message.getUserId())) {
            return MessageType.SYSTEM;
        }
        return MessageType.USER;
    }
}
