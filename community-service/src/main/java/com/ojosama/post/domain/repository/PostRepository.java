package com.ojosama.post.domain.repository;

import com.ojosama.post.domain.model.Post;
import com.ojosama.post.domain.model.PostStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, UUID> {

    //조회수 증가. MVP는 매 조회마다 호출
    //추후 Redis INCR + 주기 flush로 마이그레이션 예정
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    int incrementViewCount(@Param("id") UUID id);

    /** 특정 유저가 쓴 게시글 (소프트 삭제/BLOCKED 제외). */
    Page<Post> findByUserIdAndDeletedAtIsNullAndStatusNot(
            UUID userId, PostStatus excludedStatus, Pageable pageable);
//    @Query("SELECT p FROM Post p WHERE p.userId = :userId " +
//            "AND p.deletedAt IS NULL AND p.status != 'BLOCKED'")
//    Page<Post> findActivePostsByUserId(UUID userId, Pageable pageable);

    /** 카테고리별 게시글 (소프트 삭제/BLOCKED 제외). */
    Page<Post> findByCategoryIdAndDeletedAtIsNullAndStatusNot(
            UUID categoryId, PostStatus excludedStatus, Pageable pageable);

    /** 전체 목록 (소프트 삭제/BLOCKED 제외). */
    Page<Post> findByDeletedAtIsNullAndStatusNot(
            PostStatus excludedStatus, Pageable pageable);

    /**
     * 좋아요 수 증가. PostLike INSERT 성공 시 함께 호출.
     * 동시 요청에도 DB가 원자적으로 처리하므로 충돌하지 않는다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    int incrementLikeCount(@Param("id") UUID id);

    /**
     * 좋아요 수 감소. WHERE 조건에 {@code likeCount > 0}을 포함하여 음수 방지.
     *
     * @return 영향받은 행 수. 0이면 likeCount가 이미 0이거나 게시글이 없는 상태.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :id AND p.likeCount > 0")
    int decrementLikeCount(@Param("id") UUID id);
}
