package com.ojosama.operationservice.infrastructure.client.dto;

import java.sql.Timestamp;
import java.util.UUID;

public record ChatMessageClientResponse (
        UUID messageId,
        UUID chatRoomId,
        UUID categoryId,
        UUID userId,        // 기존에 작성자 ID를 받던 필드
        String content,
        String status,
        Timestamp createdAt
){ }
