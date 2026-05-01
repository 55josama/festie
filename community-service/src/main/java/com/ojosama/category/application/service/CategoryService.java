package com.ojosama.category.application.service;

import com.ojosama.category.application.dto.command.CreateCategoryCommand;
import com.ojosama.category.application.dto.command.DeleteCategoryCommand;
import com.ojosama.category.application.dto.result.CategoryResult;
import com.ojosama.category.domain.exception.CategoryErrorCode;
import com.ojosama.category.domain.exception.CategoryException;
import com.ojosama.category.domain.model.Category;
import com.ojosama.category.repository.CategoryRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResult create(CreateCategoryCommand cmd) {
        String normalized = normalizeOrThrow(cmd.name());
        UUID newId = UUID.randomUUID();
        if (categoryRepository.existsByNameAndDeletedAtIsNull(normalized)) {
            throw new CategoryException(CategoryErrorCode.CATEGORY_NAME_DUPLICATED);
        }
        Category category = Category.builder()
                .id(newId)
                .name(normalized)
                .build();
        Category saved = categoryRepository.save(category);
        return CategoryResult.from(saved);
    }

    @Transactional
    public void delete(DeleteCategoryCommand cmd) {
        Category category = loadAlive(cmd.categoryId());
        category.deleted(cmd.requesterId());
    }

    private Category loadAlive(UUID categoryId) {
        return categoryRepository
                .findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));
    }

    private static String normalizeOrThrow(String name) {
        if (name == null) {
            throw new CategoryException(CategoryErrorCode.INVALID_INPUT_VALUE);
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new CategoryException(CategoryErrorCode.INVALID_INPUT_VALUE);
        }
        return trimmed;
    }


}
