package com.ojosama.userservice.presentation.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponseDto<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public static <T> PageResponseDto<T> from(Page<T> page) {
        return new PageResponseDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}