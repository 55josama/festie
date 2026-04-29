package com.ojosama.comment.application.service;

import com.ojosama.comment.application.dto.CommentResult;
import com.ojosama.comment.application.dto.CreateCommentCommand;
import com.ojosama.comment.application.dto.DeleteCommentCommand;
import com.ojosama.comment.application.dto.UpdateCommentCommand;
import com.ojosama.comment.application.dto.query.CommentListQuery;
import com.ojosama.comment.domain.event.payload.CommentCreatedEvent;
import com.ojosama.comment.domain.event.payload.CommentUpdatedEvent;
import com.ojosama.comment.domain.exception.CommentErrorCode;
import com.ojosama.comment.domain.exception.CommentException;
import com.ojosama.comment.domain.model.Comment;
import com.ojosama.comment.domain.model.CommentStatus;
import com.ojosama.comment.domain.repository.CommentRepository;
import com.ojosama.common.domain.model.Content;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.OutboxEventPublisher;
import com.ojosama.post.domain.exception.PostErrorCode;
import com.ojosama.post.domain.exception.PostException;
import com.ojosama.post.domain.model.Post;
import com.ojosama.post.domain.repository.PostRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private static final String AGGREGATE_TYPE = "COMMENT";
    private static final String TOPIC_COMMENT_CREATED = "community.comment.created.v1";
    private static final String TOPIC_COMMENT_UPDATED = "community.comment.updated.v1";

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
                TOPIC_COMMENT_CREATED,
                new CommentCreatedEvent(comment.getId(),
                        comment.getPostId(),
                        comment.getUserId(),
                        comment.getParentId(),
                        comment.getContent().getValue(),
                        LocalDateTime.now()));

        return CommentResult.flat(comment);
    }

    @Transactional
    public CommentResult update(UpdateCommentCommand cmd) {
        //부모댓글 삭제해도 대댓글은 남아있으니 부모댓글 존재확인 안함
        Comment comment = loadAlive(cmd.commentId());
        if (!comment.isOwnedBy(cmd.requesterId())) {
            throw new CommentException(CommentErrorCode.COMMENT_ACCESS_DENIED);
        }

        comment.updateContent(new Content(cmd.content()));

        outbox.publish(
                AGGREGATE_TYPE, comment.getId(),
                EventType.COMMENT_UPDATED, TOPIC_COMMENT_UPDATED,
                new CommentUpdatedEvent(
                        comment.getId(),
                        comment.getPostId(),
                        comment.getUserId(),
                        comment.getContent().getValue(),
                        LocalDateTime.now()
                )
        );

        return CommentResult.flat(comment);
    }

    @Transactional
    public void delete(DeleteCommentCommand cmd) {
        Comment comment = loadAlive(cmd.commentId());
        if (!cmd.isAdmin() && !comment.isOwnedBy(cmd.requesterId())) {
            throw new CommentException(CommentErrorCode.COMMENT_ACCESS_DENIED);
        }
        comment.deleted(cmd.requesterId());
        postRepository.decrementCommentCount(comment.getPostId());
    }

    public Page<CommentResult> listByPost(CommentListQuery query) {
        Page<Comment> roots = commentRepository
                .findByPostIdAndParentIdIsNullAndDeletedAtIsNullAndStatusNot(
                        query.postId(), CommentStatus.BLOCKED, query.pageable());

        if (roots.isEmpty()) {
            return roots.map(CommentResult::flat);
        }

        List<UUID> rootIds = roots.getContent().stream()
                .map(Comment::getId).toList();

        List<Comment> replies = commentRepository
                .findByParentIdInAndDeletedAtIsNullAndStatusNotOrderByCreatedAtAsc(
                        rootIds, CommentStatus.BLOCKED);
        //WHERE parent_id IN (댓글A_id, 댓글B_id) 쿼리 하나로 모든 대댓글을 가져온다.

        Map<UUID, List<CommentResult>> repliesByParent = replies.stream()
                .collect(Collectors.groupingBy(
                        Comment::getParentId,
                        HashMap::new,
                        Collectors.mapping(CommentResult::flat, Collectors.toList())
                ));

        return roots.map(root -> CommentResult.of(
                root, repliesByParent.getOrDefault(root.getId(), List.of())));
    }

    public CommentResult getComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));
        if (comment.getDeletedAt() != null) {
            throw new CommentException(CommentErrorCode.COMMENT_NOT_FOUND);
        }
        return CommentResult.flat(comment);
    }

    private Comment loadAlive(UUID commentId) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));
        if (c.getDeletedAt() != null) {
            throw new CommentException(CommentErrorCode.COMMENT_NOT_FOUND);
        }
        return c;
    }
}
