package com.ojosama.category.presentation.controller;

import com.ojosama.category.application.dto.command.CreateCategoryCommand;
import com.ojosama.category.application.dto.command.DeleteCategoryCommand;
import com.ojosama.category.application.dto.result.CategoryResult;
import com.ojosama.category.application.service.CategoryService;
import com.ojosama.category.presentation.dto.request.CreateCategoryRequest;
import com.ojosama.category.presentation.dto.response.CategoryResponse;
import com.ojosama.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/community-categories")
@RequiredArgsConstructor
public class CategoryController {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> create(
            @Valid @RequestBody CreateCategoryRequest req) {
        CategoryResult result = categoryService.create(
                new CreateCategoryCommand(req.name()));
        return ApiResponse.created(CategoryResponse.from(result));
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> delete(
            @RequestHeader(USER_ID_HEADER) UUID requesterId,
            @PathVariable UUID categoryId) {
        categoryService.delete(new DeleteCategoryCommand(categoryId, requesterId));
        return ApiResponse.deleted();
    }


}
