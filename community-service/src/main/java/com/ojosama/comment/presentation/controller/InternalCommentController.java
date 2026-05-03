package com.ojosama.comment.presentation.controller;

import com.ojosama.comment.application.dto.CommentWriterResult;
import com.ojosama.comment.application.service.CommentService;
import com.ojosama.comment.presentation.dto.response.CommentWriterResponse;
import com.ojosama.common.response.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/comments")
@RequiredArgsConstructor
public class InternalCommentController {
    private final CommentService commentService;

    @GetMapping("/{commentId}")
    public ApiResponse<CommentWriterResponse> getComment(@PathVariable UUID commentId) {
        CommentWriterResult result = commentService.getWriter(commentId);
        return ApiResponse.success(CommentWriterResponse.from(result));
    }
}
