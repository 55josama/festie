package com.ojosama.chatservice.presentation.dto.response;

import com.ojosama.chatservice.application.dto.result.MessageResult;
import com.ojosama.chatservice.domain.model.MessageStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChangeMessageStatusResponse(
        UUID messageId,
        UUID chatRoomId,
        MessageStatus status,
        UUID processedBy,
        LocalDateTime processedAt
) {
    public static ChangeMessageStatusResponse from(MessageResult result, UUID processedBy) {
        return new ChangeMessageStatusResponse(
                result.messageId(),
                result.chatRoomId(),
                result.status(),
                processedBy,
                LocalDateTime.now()
        );
    }
}

