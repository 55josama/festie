package com.ojosama.post.domain.repository;

import com.ojosama.post.domain.model.Post;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, UUID> {

    // 특정 유저의 게시글 목록
    List<Post> findByUserIdAndDeletedAtIsNull(UUID userId);

    // 카테고리별 게시글 목록
    List<Post> findByCategoryIdAndDeletedAtIsNull(UUID categoryId);

    //조회수 증가. MVP는 매 조회마다 호출
    //추후 Redis INCR + 주기 flush로 마이그레이션 예정
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    int incrementViewCount(@Param("id") UUID id);

}
