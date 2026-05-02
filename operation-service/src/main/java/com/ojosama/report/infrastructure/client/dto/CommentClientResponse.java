package com.ojosama.report.infrastructure.client.dto;

import java.util.UUID;

public record CommentClientResponse(
        UUID id,
        UUID postId,
        UUID userId,
        UUID parentId,
        String content,
        String status,
        int likeCount
) { }
