package com.ojosama.post.presesntation.dto.response;

import com.ojosama.post.application.dto.result.PostWriterResult;
import java.util.UUID;

public record PostWriterResponse(
        UUID postId,
        UUID writerId
) {
    public static PostWriterResponse from(PostWriterResult r) {
        return new PostWriterResponse(r.postId(), r.writerId());
    }
}
