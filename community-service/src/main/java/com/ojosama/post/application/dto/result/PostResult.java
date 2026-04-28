package com.ojosama.post.application.dto.result;

import com.ojosama.post.domain.model.Post;
import com.ojosama.post.domain.model.PostStatus;
import java.time.LocalDateTime;
import java.util.UUID;

//Application Service → Presentation Layer 반환용 결과 객체.
public record PostResult(
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
    public static PostResult from(Post post) {
        return new PostResult(
                post.getId(),
                post.getUserId(),
                post.getCategoryId(),
                post.getTitle(),
                post.getContent().getValue(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getCommentCount(),
                post.getStatus(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}

