package com.ojosama.post.presesntation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.post.application.dto.result.PostWriterResult;
import com.ojosama.post.application.service.PostService;
import com.ojosama.post.presesntation.dto.response.PostWriterResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("internal/v1/post")
@RequiredArgsConstructor
public class InternalPostController {

    private final PostService postService;

    @GetMapping("/{postId}")
    public ApiResponse<PostWriterResponse> getPost(@PathVariable UUID postId) {
        PostWriterResult result = postService.getWriter(postId);
        return ApiResponse.success(PostWriterResponse.from(result));
    }
}
