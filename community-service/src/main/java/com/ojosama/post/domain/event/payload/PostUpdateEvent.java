package com.ojosama.post.domain.event.payload;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostUpdateEvent(
        UUID postId,
        UUID userId,
        UUID categoryId,
        String title,
        String content,
        LocalDateTime occurredAt
) {
}
