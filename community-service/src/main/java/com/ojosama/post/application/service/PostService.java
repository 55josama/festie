package com.ojosama.post.application.service;

import com.ojosama.common.domain.model.Content;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.OutboxEventPublisher;
import com.ojosama.post.application.dto.command.CreatePostCommand;
import com.ojosama.post.application.dto.command.UpdatePostCommand;
import com.ojosama.post.application.dto.result.PostResult;
import com.ojosama.post.domain.event.payload.PostCreatedEvent;
import com.ojosama.post.domain.event.payload.PostUpdateEvent;
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
public class PostService {
    private static final String AGGREGATE_TYPE = "POST";

    private final PostRepository postRepository;
    private final OutboxEventPublisher outbox;

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
                String.valueOf(EventType.POST_CREATED), "community.post.created.v1",
                new PostCreatedEvent(
                        post.getId(),
                        post.getUserId(),
                        post.getCategoryId(),
                        post.getTitle(),
                        post.getContent().getValue(),
                        LocalDateTime.now()
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
                String.valueOf(EventType.POST_UPDATED), "community.post.updated.v1",
                new PostUpdateEvent(
                        post.getId(),
                        post.getUserId(),
                        post.getCategoryId(),
                        post.getTitle(),
                        post.getContent().getValue(),
                        LocalDateTime.now()
                )
        );
        return PostResult.from(post);
    }

    private Post loadAlive(UUID postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                ()-> new PostException(PostErrorCode.POST_NOT_FOUND));
        if(post.getDeletedAt() != null) {
            throw new PostException(PostErrorCode.POST_NOT_FOUND);
        }
        return post;
    }


}
