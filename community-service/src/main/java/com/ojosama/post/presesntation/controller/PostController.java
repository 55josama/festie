package com.ojosama.post.presesntation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.post.application.dto.command.CreatePostCommand;
import com.ojosama.post.application.dto.command.DeletePostCommand;
import com.ojosama.post.application.dto.command.UpdatePostCommand;
import com.ojosama.post.application.dto.result.PostResult;
import com.ojosama.post.application.query.PostListQuery;
import com.ojosama.post.application.service.PostService;
import com.ojosama.post.application.service.PostLikeService;
import com.ojosama.post.presesntation.dto.request.CreatePostRequest;
import com.ojosama.post.presesntation.dto.request.UpdatePostRequest;
import com.ojosama.post.presesntation.dto.response.PostResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final PostLikeService postLikeService;

    private static final String USER_ID_HEADER = "X-User-Id";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> create(@Valid @RequestBody CreatePostRequest req,
                                            @RequestHeader(USER_ID_HEADER) UUID userId){
        PostResult result = postService.create(new CreatePostCommand(userId, req.categoryId(), req.title(), req.content()));
        return ApiResponse.created(PostResponse.from(result));
    }

    @PatchMapping("/{postId}")
    public ApiResponse<PostResponse> update(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequest req,
            @RequestHeader(USER_ID_HEADER) UUID userId) {
        PostResult result = postService.update(new UpdatePostCommand(
                postId, userId, req.categoryId(), req.title(), req.content()));
        return ApiResponse.success(PostResponse.from(result));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> delete(
            @PathVariable UUID postId,
            @RequestHeader(USER_ID_HEADER) UUID userId) {
        postService.delete(new DeletePostCommand(postId, userId, true));
        return ApiResponse.deleted();
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getDetail(@PathVariable UUID postId) {
        PostResult result = postService.getDetail(postId);
        return ApiResponse.success(PostResponse.from(result));
    }

    @GetMapping
    public ApiResponse<Page<PostResponse>> list(
            @RequestParam(required = false) UUID categoryId,
            @RequestHeader(USER_ID_HEADER) UUID userId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        PostListQuery query;
        if (categoryId != null) {
            query = PostListQuery.byCategory(categoryId, pageable);
        } else if (userId != null) {
            query = PostListQuery.byUser(userId, pageable);
        } else {
            query = PostListQuery.all(pageable);
        }
        Page<PostResult> page = postService.list(query);
        return ApiResponse.success(page.map(PostResponse::from));
    }

    @PostMapping("/{postId}/likes")
    public ApiResponse<Void> like(
            @PathVariable UUID postId,
            @RequestHeader(USER_ID_HEADER) UUID userId) {
        postLikeService.like(postId, userId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{postId}/likes")
    public ApiResponse<Void> unlike(
            @PathVariable UUID postId,
            @RequestHeader(USER_ID_HEADER) UUID userId) {
        postLikeService.unlike(postId, userId);
        return ApiResponse.success();
    }
}
