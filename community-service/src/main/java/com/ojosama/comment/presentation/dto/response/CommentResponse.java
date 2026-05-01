package com.ojosama.comment.presentation.dto.response;

import com.ojosama.comment.application.dto.CommentResult;
import com.ojosama.comment.domain.model.CommentStatus;
import com.ojosama.community.domain.model.Content;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID postId,
        UUID userId,
        UUID parentId,
        String content,
        CommentStatus status,
        int likeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentResponse> replies
) {
    public static CommentResponse from(CommentResult r) {
        boolean blocked = r.status() == CommentStatus.BLOCKED;
        List<CommentResponse> mappedReplies = r.replies() != null
                ? r.replies().stream().map(CommentResponse::from).toList()
                : List.of();
        return new CommentResponse(
                r.id(),
                r.postId(),
                r.userId(),
                r.parentId(),
                blocked ? Content.maskedText() : r.content(),
                r.status(),
                r.likeCount(),
                r.createdAt(),
                r.updatedAt(),
                mappedReplies
        );
    }
}
