package com.ojosama.comment.application.dto;

import java.util.UUID;

public record UpdateCommentCommand(
        UUID commentId,
        UUID requesterId,
        String content) {
}
