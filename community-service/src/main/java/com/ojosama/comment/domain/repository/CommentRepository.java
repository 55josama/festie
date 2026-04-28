package com.ojosama.comment.domain.repository;

import com.ojosama.comment.domain.model.Comment;
import com.ojosama.comment.domain.model.CommentStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    //추후 slice고민
    Page<Comment> findByPostIdAndParentIdIsNullAndDeletedAtIsNullAndStatusNot(
            UUID postId, CommentStatus excludedStatus, Pageable pageable);

    /** 여러 부모 댓글의 대댓글을 한 번에 조회 (N+1 방지용). */
    List<Comment> findByParentIdInAndDeletedAtIsNullAndStatusNotOrderByCreatedAtAsc(
            List<UUID> parentIds, CommentStatus excludedStatus);
}
