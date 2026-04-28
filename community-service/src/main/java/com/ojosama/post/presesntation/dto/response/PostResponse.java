package com.ojosama.post.presesntation.dto.response;

import com.ojosama.common.domain.model.Content;
import com.ojosama.post.application.dto.result.PostResult;
import com.ojosama.post.domain.model.PostStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record PostResponse(
        UUID id,
        UUID userId,
        UUID categoryId,
        String title,
        String content,
        int viewCount,
        int likeCount,
        int commentCount,
        PostStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponse from(PostResult r) {
        boolean blocked = r.status() == PostStatus.BLOCKED;
        return new PostResponse(
                r.id(),
                r.userId(),
                r.categoryId(),
                blocked ? Content.maskedText() : r.title(),
                blocked ? Content.maskedText() : r.content(),
                r.viewCount(),
                r.likeCount(),
                r.commentCount(),
                r.status(),
                r.createdAt(),
                r.updatedAt()
        );
    }
}
