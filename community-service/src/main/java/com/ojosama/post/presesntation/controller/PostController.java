package com.ojosama.post.presesntation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.post.application.dto.command.CreatePostCommand;
import com.ojosama.post.application.dto.command.DeletePostCommand;
import com.ojosama.post.application.dto.command.UpdatePostCommand;
import com.ojosama.post.application.dto.result.PostResult;
import com.ojosama.post.application.service.PostService;
import com.ojosama.post.presesntation.dto.request.CreatePostRequest;
import com.ojosama.post.presesntation.dto.request.UpdatePostRequest;
import com.ojosama.post.presesntation.dto.response.PostResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> create(@Valid @RequestBody CreatePostRequest req){
        PostResult result = postService.create(new CreatePostCommand(UUID.randomUUID(), req.categoryId(), req.title(), req.content()));
        return ApiResponse.created(PostResponse.from(result));
    }

    @PatchMapping("/{postId}")
    public ApiResponse<PostResponse> update(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequest req) {
        PostResult result = postService.update(new UpdatePostCommand(
                postId, UUID.randomUUID(), req.categoryId(), req.title(), req.content()));
        return ApiResponse.success(PostResponse.from(result));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> delete(
            @PathVariable UUID postId) {
        postService.delete(new DeletePostCommand(postId, UUID.randomUUID(), true)); //인가 완료 후 수정'
        return ApiResponse.deleted();
    }


}
