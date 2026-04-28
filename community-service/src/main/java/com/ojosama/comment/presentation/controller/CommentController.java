package com.ojosama.comment.presentation.controller;

import com.ojosama.comment.application.dto.CommentResult;
import com.ojosama.comment.application.dto.CreateCommentCommand;
import com.ojosama.comment.application.dto.DeleteCommentCommand;
import com.ojosama.comment.application.dto.UpdateCommentCommand;
import com.ojosama.comment.application.dto.query.CommentListQuery;
import com.ojosama.comment.application.service.CommentService;
import com.ojosama.comment.presentation.dto.request.CreateCommentRequest;
import com.ojosama.comment.presentation.dto.request.UpdateCommentRequest;
import com.ojosama.comment.presentation.dto.response.CommentResponse;
import com.ojosama.common.response.ApiResponse;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/v1/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> create(
            @PathVariable UUID postId,
            @Valid @RequestBody CreateCommentRequest req) {
        CommentResult result = commentService.create(new CreateCommentCommand(
                postId, UUID.randomUUID(), req.parentId(), req.content())); //인증인가 구현되면 수정
        return ApiResponse.created(CommentResponse.from(result));
    }

    @PatchMapping("/v1/comments/{commentId}")
    public ApiResponse<CommentResponse> update(
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateCommentRequest req) {
        CommentResult result = commentService.update(new UpdateCommentCommand(
                commentId, UUID.randomUUID(), req.content())); //인증인가 구현되면 수정
        return ApiResponse.success(CommentResponse.from(result));
    }

    @DeleteMapping("/v1/comments/{commentId}")
    public ApiResponse<Void> delete(
            @PathVariable UUID commentId) {
        commentService.delete(new DeleteCommentCommand(
                commentId, UUID.randomUUID(), true)); //인증인가 구현되면 수정
        return ApiResponse.deleted();
    }

    @GetMapping("/v1/posts/{postId}/comments")
    public ApiResponse<Page<CommentResponse>> list(
            @PathVariable UUID postId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<CommentResult> page = commentService.listByPost(
                new CommentListQuery(postId, pageable));
        return ApiResponse.success(page.map(CommentResponse::from));
    }
}
