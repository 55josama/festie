package com.ojosama.post.domain.repository;

import com.ojosama.post.domain.model.Post;
import com.ojosama.post.domain.model.PostStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, UUID> {

    //조회수 증가. MVP는 매 조회마다 호출 (Redis 캐시 fallback 용으로 유지)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    int incrementViewCount(@Param("id") UUID id);

//    조회수를 N 만큼 한 번에 증가 (Redis flush 용). 동일 row 에 100번 조회됐다면 UPDATE 1번으로 처리 → row lock 점유 시간 폭감.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + :delta WHERE p.id = :id")
    int incrementViewCountBy(@Param("id") UUID id, @Param("delta") long delta);

    Page<Post> findByUserIdAndDeletedAtIsNullAndStatusNot(
            UUID userId, PostStatus excludedStatus, Pageable pageable);

    Page<Post> findByCategoryIdAndDeletedAtIsNullAndStatusNot(
            UUID categoryId, PostStatus excludedStatus, Pageable pageable);

    Page<Post> findByDeletedAtIsNullAndStatusNot(
            PostStatus excludedStatus, Pageable pageable);

//    좋아요 수 증가. PostLike INSERT 성공 시 함께 호출.
//    동시 요청에도 DB가 원자적으로 처리하므로 충돌하지 않는다.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    int incrementLikeCount(@Param("id") UUID id);

//    좋아요 수 감소. WHERE 조건에 {@code likeCount > 0}을 포함하여 음수 방지.
//     @return 영향받은 행 수. 0이면 likeCount가 이미 0이거나 게시글이 없는 상태.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :id AND p.likeCount > 0")
    int decrementLikeCount(@Param("id") UUID id);

    //댓글 수 증감
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :id")
    int incrementCommentCount(@Param("id") UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 "
            + "WHERE p.id = :id AND p.commentCount > 0")
    int decrementCommentCount(@Param("id") UUID id);

    //블랙리스트 post 삭제 처리
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.status = com.ojosama.post.domain.model.PostStatus.BLINDED "
            + "WHERE p.userId = :userId AND p.deletedAt IS NULL "
            + "AND p.status <> com.ojosama.post.domain.model.PostStatus.BLINDED")
    int blindAllByUserId(@Param("userId") UUID userId);

    @Query("SELECT p.userId FROM Post p WHERE p.id = :id")
    Optional<UUID> findWriterIdById(@Param("id") UUID id);
}
