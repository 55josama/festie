package com.ojosama.comment.domain.event.payload;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentCreatedEvent(
        UUID commentId,
        UUID postId,
        UUID userId,
        UUID parentId,  // null이면 최상위 댓글
        String content,
        LocalDateTime occurredAt
) {
}
