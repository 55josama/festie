package com.ojosama.eventservice.event.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.eventservice.event.application.dto.command.CreateEventCategoryCommand;
import com.ojosama.eventservice.event.application.dto.command.UpdateEventCategoryCommand;
import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import com.ojosama.eventservice.event.application.service.EventCategoryCommandService;
import com.ojosama.eventservice.event.application.service.EventCategoryQueryService;
import com.ojosama.eventservice.event.presentation.dto.request.CreateEventCategoryRequest;
import com.ojosama.eventservice.event.presentation.dto.request.UpdateEventCategoryRequest;
import com.ojosama.eventservice.event.presentation.dto.response.EventCategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/event-categories")
@Tag(name = "행사 카테고리 API", description = "행사 카테고리 관리 (ADMIN 전용)")
public class EventCategoryController {

    private final EventCategoryCommandService eventCategoryCommandService;
    private final EventCategoryQueryService eventCategoryQueryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "카테고리 목록 조회", description = "관리자 전용 API입니다. 등록된 모든 행사 카테고리 목록을 반환합니다.")
    public ResponseEntity<ApiResponse<List<EventCategoryResponse>>> getCategories() {

        List<EventCategoryResponse> response = eventCategoryQueryService.getCategories().stream()
                .map(EventCategoryResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "카테고리 등록", description = "관리자 전용 API입니다. 새로운 행사 카테고리를 등록합니다. 카테고리 이름(name)은 필수이며 중복될 수 없습니다.")
    public ResponseEntity<ApiResponse<EventCategoryResponse>> createCategory(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateEventCategoryRequest request) {

        CreateEventCategoryCommand command = new CreateEventCategoryCommand(UUID.fromString(userId), request.name());
        EventCategoryResult result = eventCategoryCommandService.createCategory(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(EventCategoryResponse.from(result)));
    }

    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "카테고리 수정", description = "관리자 전용 API입니다. 카테고리 이름(name)을 변경합니다. 존재하지 않는 카테고리 ID로 요청 시 404를 반환합니다.")
    public ResponseEntity<ApiResponse<EventCategoryResponse>> updateCategory(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateEventCategoryRequest request) {

        UpdateEventCategoryCommand command = new UpdateEventCategoryCommand(UUID.fromString(userId), categoryId,
                request.name());
        EventCategoryResult result = eventCategoryCommandService.updateCategory(command);
        return ResponseEntity.ok(ApiResponse.success(EventCategoryResponse.from(result)));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "카테고리 삭제", description = "관리자 전용 API입니다. 해당 카테고리를 삭제합니다. 카테고리에 연결된 행사가 하나라도 존재하면 삭제할 수 없습니다. 존재하지 않는 카테고리 ID로 요청 시 404를 반환합니다.")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable UUID categoryId) {

        eventCategoryCommandService.deleteCategory(UUID.fromString(userId), categoryId);
        return ResponseEntity.noContent().build();
    }
}
