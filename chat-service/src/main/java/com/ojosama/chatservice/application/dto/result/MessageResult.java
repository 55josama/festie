package com.ojosama.chatservice.application.dto.result;

import com.ojosama.chatservice.domain.model.Message;
import com.ojosama.chatservice.domain.model.MessageStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResult(
        UUID messageId,
        UUID chatRoomId,
        UUID userId,
        String writerNickname,
        String content,
        MessageStatus status,
        LocalDateTime createdAt
) {
    public static MessageResult from(Message message) {
        return new MessageResult(
                message.getId(),
                message.getChatRoomId(),
                message.getUserId(),
                message.getWriterNickname(),
                message.getContent(),
                message.getStatus(),
                message.getCreatedAt()
        );
    }
}
