package com.ojosama.comment.application.service;

import com.ojosama.comment.domain.exception.CommentErrorCode;
import com.ojosama.comment.domain.exception.CommentException;
import com.ojosama.comment.domain.model.Comment;
import com.ojosama.comment.domain.model.CommentLike;
import com.ojosama.comment.domain.model.CommentStatus;
import com.ojosama.comment.domain.repository.CommentLikeRepository;
import com.ojosama.comment.domain.repository.CommentRepository;
import com.ojosama.post.domain.exception.PostErrorCode;
import com.ojosama.post.domain.exception.PostException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Transactional
    public int like(UUID commentId, UUID userId){
        Comment c = validateAlive(commentId);
        int likeCount = c.getLikeCount() + 1;

        if(commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw new CommentException(CommentErrorCode.ALREADY_LIKED);
        }
        try {
        CommentLike like = CommentLike.builder()
                .id(UUID.randomUUID())
                .commentId(commentId)
                .userId(userId)
                .build();
        commentLikeRepository.save(like);
        } catch (DataIntegrityViolationException e) {
            if (isDuplicateLikeViolation(e)) {
                throw new CommentException(CommentErrorCode.ALREADY_LIKED);
            }
            throw e;
        }
        commentRepository.increaseLikeCount(commentId);
        return likeCount;
//        return commentRepository.getLikeCount(commentId);
    }

    private Comment validateAlive(UUID commentId) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));
        if (c.getDeletedAt() != null) {
            throw new CommentException(CommentErrorCode.COMMENT_NOT_FOUND);
        }
        if (c.getStatus()== CommentStatus.BLOCKED) {
            throw new CommentException(CommentErrorCode.COMMENT_BLOCKED);
        }
        return c;
    }

    private boolean isDuplicateLikeViolation(DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();
        return message != null && message.contains("uq_comment_like_comment_user");
    }
}
