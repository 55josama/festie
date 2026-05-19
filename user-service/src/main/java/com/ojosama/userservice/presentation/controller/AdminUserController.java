package com.ojosama.userservice.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.common.response.PageResponse;
import com.ojosama.userservice.application.dto.query.AdminDetailUserQuery;
import com.ojosama.userservice.application.dto.result.AdminUserDetailResult;
import com.ojosama.userservice.application.service.UserAdminService;
import com.ojosama.userservice.presentation.dto.request.AdminChangeUserRoleRequestDto;
import com.ojosama.userservice.presentation.dto.request.AdminUserListRequestDto;
import com.ojosama.userservice.presentation.dto.response.AdminChangeUserRoleResponseDto;
import com.ojosama.userservice.presentation.dto.response.AdminDetailUserResponseDto;
import com.ojosama.userservice.presentation.dto.response.AdminUserListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin User", description = "관리자 회원 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final UserAdminService adminUserService;

    // 회원 목록 조회
    @GetMapping
    @Operation(
            summary = "회원 목록 조회",
            description = "관리자가 회원 목록을 조회합니다. email, name, role 조건으로 필터링할 수 있으며 공통 PageResponse 형식으로 반환합니다."
    )
    public ResponseEntity<ApiResponse<PageResponse<AdminUserListResponseDto>>> getUsers(
            @ParameterObject @ModelAttribute AdminUserListRequestDto request
    ) {
        PageResponse<AdminUserListResponseDto> response = PageResponse.from(
                adminUserService.getUsers(request.toQuery()).map(AdminUserListResponseDto::from)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    // 회원 상세 조회 1
    @GetMapping("/{userId}")
    @Operation(
            summary = "회원 상세 조회",
            description = "관리자가 특정 회원의 상세 정보를 조회합니다."
    )
    public ResponseEntity<ApiResponse<AdminDetailUserResponseDto>> getDetailUser(
            @PathVariable UUID userId
    ) {
        AdminDetailUserQuery query = new AdminDetailUserQuery(userId);
        AdminUserDetailResult result = adminUserService.getDetailUser(query);

        return ResponseEntity.ok(ApiResponse.success(AdminDetailUserResponseDto.from(result)));
    }

    // 유저 권한 수정
    @PatchMapping("/{userId}/role")
    @Operation(
            summary = "회원 권한 변경",
            description = "관리자가 특정 회원의 권한을 변경합니다."
    )

    public ResponseEntity<ApiResponse<AdminChangeUserRoleResponseDto>> changeUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminChangeUserRoleRequestDto request
    ) {
        AdminChangeUserRoleResponseDto response = AdminChangeUserRoleResponseDto.from(
                adminUserService.changeUserRole(request.toCommand(userId))
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
