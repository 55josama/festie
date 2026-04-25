package com.ojosama.post.domain.repository;

import com.ojosama.post.domain.model.Post;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, UUID> {

    // 특정 유저의 게시글 목록
    List<Post> findByUserIdAndDeletedAtIsNull(UUID userId);

    // 카테고리별 게시글 목록
    List<Post> findByCategoryIdAndDeletedAtIsNull(UUID categoryId);
}
