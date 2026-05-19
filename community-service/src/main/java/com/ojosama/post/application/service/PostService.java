package com.ojosama.post.application.service;

import com.ojosama.community.domain.event.payload.PostDeletedEvent;
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
import com.ojosama.post.infrastructure.cache.ViewCountCacheService;
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
    private final ViewCountCacheService viewCountCache;

    @Value("${spring.kafka.topic.community-moderation-requested}")
    private String moderationRequestedTopic;

    @Value("${spring.kafka.topic.post-deleted}")
    private String postDeletedTopic;

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

        // PostDeleted 이벤트 발행
        outbox.publish(
                AGGREGATE_TYPE, post.getId(),
                EventType.POST_DELETED, postDeletedTopic,
                new PostDeletedEvent(post.getId(), cmd.requesterId(), post.getDeletedAt())
        );
    }

    @Transactional(readOnly = true)
    public PostResult getDetail(UUID postId) {
        Post post = loadAlive(postId);
        // BLINDED 게시글도 200으로 응답한다 (PostResponse 에서 마스킹 처리).
        // 블라인드 상태에서는 조회수를 올리지 않음.
        if (post.isBlinded()) {
            return PostResult.from(post);
        }

        // Write-Behind: Redis INCR 만 하고 DB flush 는 스케줄러가 일괄 처리.
        // fallback(DB 직접 UPDATE)을 제거한 이유:
        //   executePipelined() 예외는 INCR 명령이 서버에서 실행된 후 응답 수신에 실패한
        //   경우(timeout 등)를 구분할 수 없다. 이 상태에서 fallback 을 실행하면
        //   Redis 캐시 + DB 에 각각 +1 되어 동일 조회에 +2가 적용되는 이중 카운트가 발생한다.
        //   조회수는 eventually consistent 가 허용되는 데이터이므로 Redis 장애 시
        //   해당 요청의 카운트를 놓치더라도 서비스 정합성에 영향이 없다고 판단하여 일시적 허용으로.
        viewCountCache.increment(postId);

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
