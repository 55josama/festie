package com.ojosama.comment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateCommentRequest(
        UUID parentId,           // null이면 최상위 댓글
        @NotBlank String content
) {
}
