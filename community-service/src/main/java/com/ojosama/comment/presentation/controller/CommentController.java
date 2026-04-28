package com.ojosama.comment.presentation.controller;

import com.ojosama.comment.application.dto.CommentResult;
import com.ojosama.comment.application.dto.CreateCommentCommand;
import com.ojosama.comment.application.service.CommentService;
import com.ojosama.comment.presentation.dto.request.CreateCommentRequest;
import com.ojosama.comment.presentation.dto.response.CommentResponse;
import com.ojosama.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
                postId, UUID.randomUUID(), req.parentId(), req.content()));
        return ApiResponse.created(CommentResponse.from(result));
    }

}
