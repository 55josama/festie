package com.ojosama.post.application.query;

import java.util.UUID;
import org.springframework.data.domain.Pageable;

//게시글 목록 조회 쿼리
public record PostListQuery(
        UUID categoryId,
        UUID userId,
        Pageable pageable
) {
    public static PostListQuery all(Pageable pageable) {
        return new PostListQuery(null, null, pageable);
    }

    public static PostListQuery byCategory(UUID categoryId, Pageable pageable) {
        return new PostListQuery(categoryId, null, pageable);
    }

    public static PostListQuery byUser(UUID userId, Pageable pageable) {
        return new PostListQuery(null, userId, pageable);
    }
}

