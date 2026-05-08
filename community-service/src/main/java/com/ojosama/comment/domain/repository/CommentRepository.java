package com.ojosama.comment.domain.repository;

import com.ojosama.comment.domain.model.Comment;
import com.ojosama.comment.domain.model.CommentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    //추후 slice고민
    Page<Comment> findByPostIdAndParentIdIsNullAndDeletedAtIsNullAndStatusNot(
            UUID postId, CommentStatus excludedStatus, Pageable pageable);

    /** 여러 부모 댓글의 대댓글을 한 번에 조회 (N+1 방지용). */
    List<Comment> findByParentIdInAndDeletedAtIsNullAndStatusNotOrderByCreatedAtAsc(
            List<UUID> parentIds, CommentStatus excludedStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :id")
    void incrementLikeCount(@Param("id") UUID Id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :id AND c.likeCount > 0")
    void decrementLikeCount(@Param("id") UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Comment c SET c.status = com.ojosama.comment.domain.model.CommentStatus.BLINDED "
            + "WHERE c.userId = :userId AND c.deletedAt IS NULL "
            + "AND c.status <> com.ojosama.comment.domain.model.CommentStatus.BLINDED")
    int blindAllByUserId(@Param("userId") UUID userId);

    @Query("SELECT c.userId FROM Comment c WHERE c.id = :id")
    Optional<UUID> findWriterIdById(@Param("id") UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Comment c SET c.deletedAt = :deletedAt "
            + "WHERE c.postId = :postId AND c.deletedAt IS NULL")
    int softDeleteAllByPostId(@Param("postId") java.util.UUID postId,
                              @Param("deletedAt") LocalDateTime deletedAt);

}
