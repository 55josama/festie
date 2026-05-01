package com.ojosama.category.presentation.controller;

import com.ojosama.category.application.dto.command.CreateCategoryCommand;
import com.ojosama.category.application.dto.command.DeleteCategoryCommand;
import com.ojosama.category.application.dto.command.UpdateCategoryCommand;
import com.ojosama.category.application.dto.result.CategoryResult;
import com.ojosama.category.application.service.CategoryService;
import com.ojosama.category.presentation.dto.request.CreateCategoryRequest;
import com.ojosama.category.presentation.dto.request.UpdateCategoryRequest;
import com.ojosama.category.presentation.dto.response.CategoryResponse;
import com.ojosama.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> update(
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest req) {
        CategoryResult result = categoryService.update(
                new UpdateCategoryCommand(categoryId, req.name()));
        return ApiResponse.success(CategoryResponse.from(result));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(
            @RequestHeader(USER_ID_HEADER) UUID requesterId,
            @PathVariable UUID categoryId) {
        categoryService.delete(new DeleteCategoryCommand(categoryId, requesterId));
        return ApiResponse.deleted();
    }

    @GetMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> getDetail(@PathVariable UUID categoryId) {
        CategoryResult result = categoryService.getDetail(categoryId);
        return ApiResponse.success(CategoryResponse.from(result));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<CategoryResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<CategoryResult> page = categoryService.list(pageable);
        return ApiResponse.success(page.map(CategoryResponse::from));
    }

}
