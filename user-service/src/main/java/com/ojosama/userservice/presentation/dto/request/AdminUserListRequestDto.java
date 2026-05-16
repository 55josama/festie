package com.ojosama.userservice.presentation.dto.request;

import com.ojosama.userservice.application.dto.query.AdminUserListQuery;
import com.ojosama.userservice.domain.model.UserRole;

public record AdminUserListRequestDto(
        String email,
        String name,
        UserRole role,
        Integer page,
        Integer size
) {
    public AdminUserListQuery toQuery() {
        return new AdminUserListQuery(
                email == null || email.isBlank() ? null : email.trim(),
                name == null || name.isBlank() ? null : name.trim(),
                role,
                page == null || page < 0 ? 0 : page,
                size == null || size <= 0 ? 20 : size
        );
    }
}
