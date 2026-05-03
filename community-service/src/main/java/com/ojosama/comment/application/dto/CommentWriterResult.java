package com.ojosama.comment.application.dto;

import java.util.UUID;

public record CommentWriterResult(
        UUID commentId,
        UUID writerId
) {
}
