package com.ojosama.category.application.dto.command;

import java.util.UUID;

public record DeleteCategoryCommand(
        UUID categoryId,
        UUID requesterId
) {
}
