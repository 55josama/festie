package com.ojosama.comment.application.dto;

import java.util.UUID;

public record DeleteCommentCommand(
        UUID commentId,
        UUID requesterId,
        boolean isAdmin
) {
}
