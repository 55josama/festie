package com.ojosama.operationservice.infrastructure.client;

import com.ojosama.operationservice.infrastructure.client.dto.PostClientResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "community-service")
public interface CommunityClient {
    @GetMapping("/v1/posts/{postId}")
    PostClientResponse getPostWriter(@PathVariable("postId") UUID userId);

    @GetMapping("/v1/comments/{commentId}")
    PostClientResponse getCommentWriter(@PathVariable("commentId") UUID userId);
}
