package com.ojosama.comment.domain.event.payload;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentUpdatedEvent(
        UUID commentId,
        UUID postId,
        UUID userId,
        String content,
        LocalDateTime occurredAt
){
}
