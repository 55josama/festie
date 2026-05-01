package com.ojosama.chatservice.presentation.dto.response;

import com.ojosama.chatservice.application.dto.result.MessageResult;
import com.ojosama.chatservice.domain.model.MessageStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record MessageWsResponse(
        UUID messageId,
        UUID chatRoomId,
        UUID userId,
        String writerNickname,
        String content,
        MessageStatus status,
        LocalDateTime createdAt
) {
    public static MessageWsResponse from(MessageResult result) {
        return new MessageWsResponse(
                result.messageId(),
                result.chatRoomId(),
                result.userId(),
                result.writerNickname(),
                result.content(),
                result.status(),
                result.createdAt()
        );
    }
}
