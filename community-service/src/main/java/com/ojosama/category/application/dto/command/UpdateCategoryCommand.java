package com.ojosama.category.application.dto.command;

import java.util.UUID;

public record UpdateCategoryCommand(
        UUID categoryId,
        String name
) {
}
