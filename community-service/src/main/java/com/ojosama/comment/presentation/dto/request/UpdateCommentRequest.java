package com.ojosama.comment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateCommentRequest(
        @NotBlank String content
) {
}
