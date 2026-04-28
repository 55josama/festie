package com.ojosama.post.application.dto.command;

import java.util.UUID;

public record DeletePostCommand(
        UUID postId,
        UUID requesterId,
        boolean isAdmin
) {
}
