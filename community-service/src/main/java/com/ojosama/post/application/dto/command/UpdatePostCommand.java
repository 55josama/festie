package com.ojosama.post.application.dto.command;

import java.util.UUID;

public record UpdatePostCommand(
        UUID postId,
        UUID requesterId,
        UUID categoryId,
        String title,
        String content
) {
}
