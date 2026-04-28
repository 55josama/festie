package com.ojosama.comment.application.service;

import com.ojosama.comment.application.dto.CommentResult;
import com.ojosama.comment.application.dto.CreateCommentCommand;
import com.ojosama.comment.domain.event.payload.CommentCreatedEvent;
import com.ojosama.comment.domain.exception.CommentErrorCode;
import com.ojosama.comment.domain.exception.CommentException;
import com.ojosama.comment.domain.model.Comment;
import com.ojosama.comment.domain.repository.CommentRepository;
import com.ojosama.common.domain.model.Content;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.OutboxEventPublisher;
import com.ojosama.post.domain.exception.PostErrorCode;
import com.ojosama.post.domain.exception.PostException;
import com.ojosama.post.domain.model.Post;
import com.ojosama.post.domain.repository.PostRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private static final String AGGREGATE_TYPE = "COMMENT";

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final OutboxEventPublisher outbox;

    @Transactional
    public CommentResult create(CreateCommentCommand cmd) {
        Post post = postRepository.findById(cmd.postId()).orElseThrow(
                () -> new PostException(PostErrorCode.POST_NOT_FOUND));
        if (post.getDeletedAt() != null || post.isBlocked()) {
            throw new PostException(PostErrorCode.POST_NOT_FOUND);
        }

        UUID newId = UUID.randomUUID();
        Comment comment;
        if (cmd.parentId() == null) {
            comment = Comment.createRoot(newId, cmd.postId(), cmd.userId(), new Content(cmd.content()));
        } else {
            Comment parent = commentRepository.findById(cmd.parentId()).orElseThrow(() -> new CommentException(
                    CommentErrorCode.PARENT_COMMENT_NOT_FOUND));
            if (parent.getDeletedAt() != null) {
                throw new CommentException(CommentErrorCode.PARENT_COMMENT_NOT_FOUND);
            }
            if (!parent.getPostId().equals(cmd.postId())) {
                throw new CommentException(CommentErrorCode.POST_MISMATCH);
            }
            // 2-depth 강제는 Comment.createReply 내부에서 검증 DDD
            comment = Comment.createReply(newId, cmd.userId(), new Content(cmd.content()), parent);
        }
        commentRepository.save(comment);
        postRepository.incrementCommentCount(cmd.postId());

        outbox.publish(AGGREGATE_TYPE, comment.getId(), EventType.COMMENT_CREATED,
                "community.comment.created.v1",
                new CommentCreatedEvent(comment.getId(),
                        comment.getPostId(),
                        comment.getUserId(),
                        comment.getParentId(),
                        comment.getContent().getValue(),
                        LocalDateTime.now()));

        return CommentResult.flat(comment);
    }

}
