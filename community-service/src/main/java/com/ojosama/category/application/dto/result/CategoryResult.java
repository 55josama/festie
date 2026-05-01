package com.ojosama.category.application.dto.result;

import com.ojosama.category.domain.model.Category;
import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResult(
        UUID id,
        String name,
        UUID createdBy,
        UUID updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CategoryResult from(Category c) {
        return new CategoryResult(
                c.getId(),
                c.getName(),
                c.getCreatedBy(),
                c.getUpdatedBy(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
