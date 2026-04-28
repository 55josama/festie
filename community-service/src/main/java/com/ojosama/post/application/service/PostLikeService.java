package com.ojosama.post.application.service;

import com.ojosama.post.domain.exception.PostErrorCode;
import com.ojosama.post.domain.exception.PostException;
import com.ojosama.post.domain.model.Post;
import com.ojosama.post.domain.model.PostLike;
import com.ojosama.post.domain.repository.PostLikeRepository;
import com.ojosama.post.domain.repository.PostRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public void like(UUID postId, UUID userId) {
        validatePostAlive(postId);

        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new PostException(PostErrorCode.ALREADY_LIKED);
        }

        try {
            PostLike like = PostLike.builder()
                            .id(UUID.randomUUID())
                            .postId(postId)
                            .userId(userId)
                            .build();
            postLikeRepository.save(like);
        } catch (DataIntegrityViolationException e) {
            if (isDuplicateLikeViolation(e)) {
                throw new PostException(PostErrorCode.ALREADY_LIKED);
            }
            throw e; //중복 키 위반만 선별해서 변환하고 나머지는 그대로 전파해야 원인 파악이 가능합니다.
        }

        postRepository.incrementLikeCount(postId);
    }

    @Transactional
    public void unlike(UUID postId, UUID userId){
        validatePostAlive(postId);

        int deleted = postLikeRepository.deleteByPostIdAndUserId(postId, userId);
        if (deleted == 0) {
            throw new PostException(PostErrorCode.NOT_LIKED);
        }
        postRepository.decrementLikeCount(postId);
    }

    private void validatePostAlive(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
        if (post.getDeletedAt() != null) {
            throw new PostException(PostErrorCode.POST_NOT_FOUND);
        }
        if (post.isBlocked()) {
            throw new PostException(PostErrorCode.POST_BLOCKED);
        }
    }

    private boolean isDuplicateLikeViolation(DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();
        return message != null && message.contains("user_id");
    }
}
