package com.ojosama.post.presesntation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.post.application.dto.command.CreatePostCommand;
import com.ojosama.post.application.dto.result.PostResult;
import com.ojosama.post.application.service.PostService;
import com.ojosama.post.presesntation.dto.request.CreatePostRequest;
import com.ojosama.post.presesntation.dto.response.PostResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
}
