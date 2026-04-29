package com.ojosama.userservice.presentation.controller;

import com.ojosama.userservice.application.dto.query.AdminDetailUserQuery;
import com.ojosama.userservice.application.dto.result.AdminUserDetailResult;
import com.ojosama.userservice.application.service.UserAdminService;
import com.ojosama.userservice.presentation.dto.request.AdminChangeUserRoleRequestDto;
import com.ojosama.userservice.presentation.dto.request.AdminUserListRequestDto;
import com.ojosama.userservice.presentation.dto.response.AdminChangeUserRoleResponseDto;
import com.ojosama.userservice.presentation.dto.response.AdminDetailUserResponseDto;
import com.ojosama.userservice.presentation.dto.response.AdminUserListResponseDto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
public class AdminUserController {

    private final UserAdminService adminUserService;

    //회원 목록 조회
    @GetMapping
    public Page<AdminUserListResponseDto> getUsers(
            @ModelAttribute AdminUserListRequestDto request
    ) {
        return adminUserService.getUsers(request.toQuery())
                .map(AdminUserListResponseDto::from);
    }

    //회원 상세 조회
    @GetMapping("/{userId}")
    public AdminDetailUserResponseDto getDetailUser(
            @PathVariable UUID userId
    ) {
        AdminDetailUserQuery query = new AdminDetailUserQuery(userId);
        AdminUserDetailResult result = adminUserService.getDetailUser(query);

        return AdminDetailUserResponseDto.from(result);
    }

    //유저 권한 수정
    @PatchMapping("/{userId}")
    public AdminChangeUserRoleResponseDto changeUserRole(
            @PathVariable UUID userId,
            @RequestBody AdminChangeUserRoleRequestDto request
    ) {
        return AdminChangeUserRoleResponseDto.from(
                adminUserService.changeUserRole(request.toCommand(userId))
        );
    }
}
