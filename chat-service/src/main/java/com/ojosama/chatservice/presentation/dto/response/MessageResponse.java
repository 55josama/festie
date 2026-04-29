package com.ojosama.chatservice.presentation.dto.response;

import com.ojosama.chatservice.application.dto.result.MessageResult;
import com.ojosama.chatservice.domain.model.MessageStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID messageId,
        UUID chatRoomId,
        UUID userId,
        String content,
        MessageStatus status,
        LocalDateTime createdAt
) {
    public static MessageResponse from(MessageResult result) {
        return new MessageResponse(
                result.messageId(),
                result.chatRoomId(),
                result.userId(),
                result.content(),
                result.status(),
                result.createdAt()
        );
    }
}
