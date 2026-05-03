package com.ojosama.post.application.dto.result;

import java.util.UUID;

public record PostWriterResult(
        UUID postId,
        UUID writerId
){}
