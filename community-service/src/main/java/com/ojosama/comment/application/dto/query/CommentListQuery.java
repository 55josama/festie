package com.ojosama.comment.application.dto.query;

import java.util.UUID;
import org.springframework.data.domain.Pageable;

public record CommentListQuery(
        UUID postId,
        Pageable pageable
) {
}
