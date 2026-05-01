package com.ojosama.category.presentation.dto.response;

import com.ojosama.category.application.dto.result.CategoryResult;
import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        UUID createdBy,
        UUID updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CategoryResponse from(CategoryResult r) {
        return new CategoryResponse(
                r.id(),
                r.name(),
                r.createdBy(),
                r.updatedBy(),
                r.createdAt(),
                r.updatedAt()
        );
    }
}
