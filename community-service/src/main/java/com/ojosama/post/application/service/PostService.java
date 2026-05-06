package com.ojosama.post.application.service;

import com.ojosama.community.domain.model.Content;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.OutboxEventPublisher;
import com.ojosama.community.domain.payload.ModerationRequestedEvent;
import com.ojosama.community.domain.payload.TargetType;
import com.ojosama.post.application.dto.command.CreatePostCommand;
import com.ojosama.post.application.dto.command.DeletePostCommand;
import com.ojosama.post.application.dto.command.UpdatePostCommand;
import com.ojosama.post.application.dto.result.PostResult;
import com.ojosama.post.application.dto.result.PostWriterResult;
import com.ojosama.post.application.query.PostListQuery;
import com.ojosama.post.domain.exception.PostErrorCode;
import com.ojosama.post.domain.exception.PostException;
import com.ojosama.post.domain.model.Post;
import com.ojosama.post.domain.model.PostStatus;
import com.ojosama.post.domain.repository.PostRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private static final String AGGREGATE_TYPE = "POST";

    private final PostRepository postRepository;
    private final OutboxEventPublisher outbox;

    @Value("${spring.kafka.topic.community-moderation-requested}")
    private String moderationRequestedTopic;

    @Transactional
    public PostResult create(CreatePostCommand cmd){
        UUID id = UUID.randomUUID();
        Post post = Post.create(
                id,
                cmd.userId(),
                cmd.categoryId(),
                cmd.title(),
                new Content(cmd.content())
        );
        postRepository.save(post);

        outbox.publish(
                AGGREGATE_TYPE, post.getId(),
                EventType.COMMUNITY_MODERATION_REQUESTED, moderationRequestedTopic,
                ModerationRequestedEvent.of(
                        post.getId(),
                        post.getUserId(),
                        TargetType.POST,
                        post.getContent().getValue()
                )
        );
        return PostResult.from(post);
    }

    @Transactional
    public PostResult update(UpdatePostCommand cmd){
        Post post = loadAlive(cmd.postId());
        if (!post.isOwnedBy(cmd.requesterId())){
            throw new PostException(PostErrorCode.POST_ACCESS_DENIED);
        }
        post.update(cmd.title(), new Content(cmd.content()), cmd.categoryId());

        outbox.publish(
                AGGREGATE_TYPE, post.getId(),
                EventType.COMMUNITY_MODERATION_REQUESTED, moderationRequestedTopic,
                ModerationRequestedEvent.of(
                        post.getId(),
                        post.getUserId(),
                        TargetType.POST,
                        post.getContent().getValue()
                )
        );
        return PostResult.from(post);
    }

    @Transactional
    public void delete(DeletePostCommand cmd) {
        Post post = loadAlive(cmd.postId());
        if( !cmd.isAdmin() && !post.isOwnedBy(cmd.requesterId())){
            throw new PostException(PostErrorCode.POST_ACCESS_DENIED);
        }
        post.deleted(cmd.requesterId());
    }

    @Transactional
    public PostResult getDetail(UUID postId) {
        Post post = loadAlive(postId);
        // BLOCKED 게시글도 200으로 응답한다 (PostResponse에서 마스킹 처리).
        // 단, 조회수는 증가시키지 않음 — 차단된 게시글에 어뷰징성 조회수 발생 방지.
        if (post.isBlinded()) {
            return PostResult.from(post);
        }
        int affected = postRepository.incrementViewCount(postId);
        if (affected == 0) {
            throw new PostException(PostErrorCode.POST_NOT_FOUND);
        }
        return adjustViewCountForResponse(post, 1);
    }

    public Page<PostResult> list(PostListQuery query) {
        Page<Post> posts;
        if (query.categoryId() != null) {
            posts = postRepository.findByCategoryIdAndDeletedAtIsNullAndStatusNot(
                    query.categoryId(), PostStatus.BLINDED, query.pageable());
        } else if (query.userId() != null) {
            posts = postRepository.findByUserIdAndDeletedAtIsNullAndStatusNot(
                    query.userId(), PostStatus.BLINDED, query.pageable());
        } else {
            posts = postRepository.findByDeletedAtIsNullAndStatusNot(
                    PostStatus.BLINDED, query.pageable());
        }
        return posts.map(PostResult::from);
    }

    @Transactional(readOnly = true)
    public PostWriterResult getWriter(UUID postId) {
        UUID writerId = postRepository.findWriterIdById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
        return new PostWriterResult(postId, writerId);
    }

    private Post loadAlive(UUID postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                ()-> new PostException(PostErrorCode.POST_NOT_FOUND));
        if(post.getDeletedAt() != null) {
            throw new PostException(PostErrorCode.POST_NOT_FOUND);
        }
        return post;
    }

    private PostResult adjustViewCountForResponse(Post post, int delta) {
        return new PostResult(
                post.getId(),
                post.getUserId(),
                post.getCategoryId(),
                post.getTitle(),
                post.getContent() != null ? post.getContent().getValue() : null,
                post.getViewCount() + delta,
                post.getLikeCount(),
                post.getCommentCount(),
                post.getStatus(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }


}
