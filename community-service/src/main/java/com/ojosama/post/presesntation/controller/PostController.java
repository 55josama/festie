package com.ojosama.post.presesntation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.common.response.PageResponse;
import com.ojosama.post.application.dto.command.CreatePostCommand;
import com.ojosama.post.application.dto.command.DeletePostCommand;
import com.ojosama.post.application.dto.command.UpdatePostCommand;
import com.ojosama.post.application.dto.result.PostResult;
import com.ojosama.post.application.query.PostListQuery;
import com.ojosama.post.application.service.PostLikeService;
import com.ojosama.post.application.service.PostService;
import com.ojosama.post.presesntation.dto.request.CreatePostRequest;
import com.ojosama.post.presesntation.dto.request.UpdatePostRequest;
import com.ojosama.post.presesntation.dto.response.PostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "게시글", description = "게시글 CRUD 및 좋아요 API")
@RestController
@RequestMapping("v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;

    @Operation(summary = "게시글 작성", description = "새 게시글을 작성합니다. 로그인한 사용자만 가능합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<PostResponse> create(
            @Valid @RequestBody CreatePostRequest req,
            @AuthenticationPrincipal UUID userId) {
        PostResult result = postService.create(new CreatePostCommand(
                userId, req.categoryId(), req.title(), req.content()));
        return ApiResponse.created(PostResponse.from(result));
    }

    @Operation(summary = "게시글 수정", description = "본인이 작성한 게시글을 수정합니다.")
    @PatchMapping("/{postId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<PostResponse> update(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequest req,
            @AuthenticationPrincipal UUID userId) {
        PostResult result = postService.update(new UpdatePostCommand(
                postId, userId, req.categoryId(), req.title(), req.content()));
        return ApiResponse.success(PostResponse.from(result));
    }

    @Operation(summary = "게시글 삭제", description = "본인이 작성한 게시글을 삭제합니다. 관리자는 모든 게시글 삭제 가능합니다.")
    @DeleteMapping("/{postId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<Void> delete(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UUID userId,
            Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        postService.delete(new DeletePostCommand(postId, userId, isAdmin));
        return ApiResponse.deleted();
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다. 비로그인도 가능합니다.")
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getDetail(@PathVariable UUID postId) {
        PostResult result = postService.getDetail(postId);
        return ApiResponse.success(PostResponse.from(result));
    }

    @Operation(
            summary = "게시글 목록 조회",
            description = "게시글 목록을 조회합니다. 비로그인도 가능합니다. <br>" +
                    "categoryId 지정 시 카테고리 필터링, userId 지정 시 특정 유저의 게시글만 조회합니다."
    )
    @GetMapping
    public ApiResponse<PageResponse<PostResponse>> list(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID userId,
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
        return ApiResponse.success(PageResponse.from(page.map(PostResponse::from)));
    }

    @Operation(summary = "게시글 좋아요", description = "게시글에 좋아요를 누릅니다. 중복 좋아요는 불가합니다.")
    @PostMapping("/{postId}/likes")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<Void> like(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UUID userId) {
        postLikeService.like(postId, userId);
        return ApiResponse.success();
    }

    @Operation(summary = "게시글 좋아요 취소", description = "게시글 좋아요를 취소합니다.")
    @DeleteMapping("/{postId}/likes")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<Void> unlike(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UUID userId) {
        postLikeService.unlike(postId, userId);
        return ApiResponse.success();
    }
}
