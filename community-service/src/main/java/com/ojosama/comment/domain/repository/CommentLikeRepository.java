package com.ojosama.comment.domain.repository;

import com.ojosama.comment.domain.model.CommentLike;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {
    boolean existsByCommentIdAndUserId(UUID commentId, UUID userId);
}
