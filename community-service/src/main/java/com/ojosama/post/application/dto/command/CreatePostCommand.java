package com.ojosama.post.application.dto.command;

import java.util.UUID;

public record CreatePostCommand(
        UUID userId,
        UUID categoryId,
        String title,
        String content
) {
}
