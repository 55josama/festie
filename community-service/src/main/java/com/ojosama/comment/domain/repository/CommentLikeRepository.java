package com.ojosama.comment.domain.repository;

import com.ojosama.comment.domain.model.CommentLike;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {
    boolean existsByCommentIdAndUserId(UUID commentId, UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CommentLike cl WHERE cl.commentId = :commentId AND cl.userId = :userId")
    int deleteByCommentIdAndUserId(@Param("commentId") UUID commentId, @Param("userId") UUID userId);
}
