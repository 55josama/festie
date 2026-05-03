package com.ojosama.comment.presentation.dto.response;

import com.ojosama.comment.application.dto.CommentWriterResult;
import java.util.UUID;

public record CommentWriterResponse(
        UUID commentId,
        UUID writerId
) {
    public static CommentWriterResponse from(CommentWriterResult r) {
        return new CommentWriterResponse(r.commentId(), r.writerId());
    }
}
