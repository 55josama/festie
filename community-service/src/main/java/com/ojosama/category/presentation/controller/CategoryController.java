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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "커뮤니티 카테고리", description = "커뮤니티 카테고리 관리 API (관리자 전용)")
@RestController
@RequestMapping("/v1/community-categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 생성", description = "새 커뮤니티 카테고리를 생성합니다. 관리자만 가능합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> create(
            @Valid @RequestBody CreateCategoryRequest req) {
        CategoryResult result = categoryService.create(
                new CreateCategoryCommand(req.name()));
        return ApiResponse.created(CategoryResponse.from(result));
    }

    @Operation(summary = "카테고리 수정", description = "카테고리 이름을 수정합니다. 관리자만 가능합니다.")
    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> update(
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest req) {
        CategoryResult result = categoryService.update(
                new UpdateCategoryCommand(categoryId, req.name()));
        return ApiResponse.success(CategoryResponse.from(result));
    }

    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제합니다. 관리자만 가능합니다.")
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UUID userId) {
        categoryService.delete(new DeleteCategoryCommand(categoryId, userId));
        return ApiResponse.deleted();
    }

    @Operation(summary = "카테고리 상세 조회", description = "카테고리 상세 정보를 조회합니다. 비로그인도 가능합니다.")
    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> getDetail(@PathVariable UUID categoryId) {
        CategoryResult result = categoryService.getDetail(categoryId);
        return ApiResponse.success(CategoryResponse.from(result));
    }

    @Operation(summary = "카테고리 목록 조회", description = "전체 커뮤니티 카테고리 목록을 조회합니다. 비로그인도 가능합니다.")
    @GetMapping
    public ApiResponse<Page<CategoryResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<CategoryResult> page = categoryService.list(pageable);
        return ApiResponse.success(page.map(CategoryResponse::from));
    }
}
