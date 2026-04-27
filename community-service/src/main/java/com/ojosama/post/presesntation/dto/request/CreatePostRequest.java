package com.ojosama.post.presesntation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.antlr.v4.runtime.misc.NotNull;

public record CreatePostRequest(
        @NotNull UUID categoryId,
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content
) {
}
