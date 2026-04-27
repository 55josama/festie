package com.ojosama.userservice.presentation.dto.request;

import com.ojosama.userservice.application.dto.query.AdminUserListQuery;

public record AdminUserListRequestDto(
        Integer page,
        Integer size
) {
    public AdminUserListQuery toQuery() {
        return new AdminUserListQuery(
                page == null || page < 0 ? 0 : page,
                size == null || size <= 0 ? 20 : size
        );
    }
}