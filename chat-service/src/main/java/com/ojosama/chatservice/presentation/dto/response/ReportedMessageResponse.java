package com.ojosama.chatservice.presentation.dto.response;

import com.ojosama.chatservice.application.dto.result.ReportedMessageResult;
import com.ojosama.chatservice.domain.model.MessageStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReportedMessageResponse(
        UUID messageId,
        UUID chatRoomId,
        String category,
        UUID userId,
        String content,
        MessageStatus status,
        LocalDateTime createdAt
) {
    public static ReportedMessageResponse from(ReportedMessageResult result) {
        return new ReportedMessageResponse(
                result.messageId(),
                result.chatRoomId(),
                result.category(),
                result.userId(),
                result.content(),
                result.status(),
                result.createdAt()
        );
    }
}
