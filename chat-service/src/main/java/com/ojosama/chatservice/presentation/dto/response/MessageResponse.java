package com.ojosama.chatservice.presentation.dto.response;

import com.ojosama.chatservice.application.dto.result.MessageResult;
import com.ojosama.chatservice.domain.model.MessageStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID messageId,
        UUID chatRoomId,
        UUID userId,
        String writerNickname,
        String content,
        MessageStatus status,
        LocalDateTime createdAt
) {
    private static final String BLINDED_CONTENT = "블라인드 처리된 메시지입니다.";

    public static MessageResponse from(MessageResult result) {
        String content = result.status() == MessageStatus.BLINDED ? BLINDED_CONTENT : result.content();
        return new MessageResponse(
                result.messageId(),
                result.chatRoomId(),
                result.userId(),
                result.writerNickname(),
                content,
                result.status(),
                result.createdAt()
        );
    }
}
