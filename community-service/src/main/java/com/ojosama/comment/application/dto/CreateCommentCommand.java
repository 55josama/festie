package com.ojosama.comment.application.dto;

import java.util.UUID;

public record CreateCommentCommand(
        UUID postId,
        UUID userId,
        UUID parentId,  // null이면 최상위 댓글
        String content
) {
}
