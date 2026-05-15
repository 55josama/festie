package com.ojosama.userservice.application.dto.query;

import com.ojosama.userservice.domain.model.UserRole;

public record AdminUserListQuery(
        String email,
        String name,
        UserRole role,
        int page,
        int size
) {
}
