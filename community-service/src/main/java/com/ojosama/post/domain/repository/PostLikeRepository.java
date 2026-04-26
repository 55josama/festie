package com.ojosama.post.domain.repository;

import com.ojosama.post.domain.model.PostLike;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

    // 좋아요 중복 체크용
    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    // 좋아요 취소용
    Optional<PostLike> findByPostIdAndUserId(UUID postId, UUID userId);
}
