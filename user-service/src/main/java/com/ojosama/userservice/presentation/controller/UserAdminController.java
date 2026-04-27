package com.ojosama.userservice.presentation.controller;

import com.ojosama.userservice.application.service.UserAdminService;
import com.ojosama.userservice.presentation.dto.request.AdminUserListRequestDto;
import com.ojosama.userservice.presentation.dto.response.AdminUserListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users/admin")
public class UserAdminController {

    private final UserAdminService userAdminService;

    //회원 목록 조회
    @Transactional(readOnly = true)
    @GetMapping
    public Page<AdminUserListResponseDto> getUsers(
            @ModelAttribute AdminUserListRequestDto request
    ) {
        return userAdminService.getUsers(request.toQuery())
                .map(AdminUserListResponseDto::from);
    }
}
