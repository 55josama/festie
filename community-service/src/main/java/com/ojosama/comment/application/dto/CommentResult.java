package com.ojosama.comment.application.dto;

import com.ojosama.comment.domain.model.Comment;
import com.ojosama.comment.domain.model.CommentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CommentResult(
        UUID id,
        UUID postId,
        UUID userId,
        UUID parentId,
        String content,
        CommentStatus status,
        int likeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentResult> replies
) {
    public static CommentResult of(Comment c, List<CommentResult> replies) {
        return new CommentResult(
                c.getId(),
                c.getPostId(),
                c.getUserId(),
                c.getParentId(),
                c.getContent() != null ? c.getContent().getValue() : null,
                c.getStatus(),
                c.getLikeCount(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                replies
        );
    }

    public static CommentResult flat(Comment c) {
        return of(c, List.of());
    }
}
