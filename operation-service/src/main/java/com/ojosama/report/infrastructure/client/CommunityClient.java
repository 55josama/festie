package com.ojosama.report.infrastructure.client;

import com.ojosama.report.infrastructure.client.dto.CommentClientResponse;
import com.ojosama.report.infrastructure.client.dto.PostClientResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "community-service")
public interface CommunityClient {
    @GetMapping("/internal/v1/posts/{postId}")
    PostClientResponse getPostWriter(@PathVariable("postId") UUID writerId);

    @GetMapping("/internal/v1/comments/{commentId}")
    CommentClientResponse getCommentWriter(@PathVariable("commentId") UUID writerId);
}
