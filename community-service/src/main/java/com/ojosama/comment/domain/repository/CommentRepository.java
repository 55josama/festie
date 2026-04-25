package com.ojosama.comment.domain.repository;

import com.ojosama.comment.domain.model.Comment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // 게시글의 댓글 목록 (삭제되지 않은 것만)
    List<Comment> findByPostIdAndDeletedAtIsNull(UUID postId);

    // 대댓글 목록
    List<Comment> findByParentIdAndDeletedAtIsNull(UUID parentId);
}
