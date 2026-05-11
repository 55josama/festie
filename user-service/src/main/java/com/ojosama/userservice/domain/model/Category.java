package com.ojosama.userservice.domain.model;

import com.ojosama.userservice.domain.exception.UserErrorCode;
import com.ojosama.userservice.domain.exception.UserException;
import java.util.Locale;

public enum Category {
    CONCERT(UserRole.CONCERT_MANAGER),
    FESTIVAL(UserRole.FESTIVAL_MANAGER),
    FANMEETING(UserRole.FANMEETING_MANAGER),
    POPUP(UserRole.POPUP_MANAGER);

    private final UserRole managerRole;

    Category(UserRole managerRole) {
        this.managerRole = managerRole;
    }

    public UserRole getManagerRole() {
        return managerRole;
    }

    public static Category from(String value) {
        if (value == null || value.isBlank()) {
            throw new UserException(UserErrorCode.INVALID_CATEGORY);
        }

        try {
            return Category.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new UserException(UserErrorCode.INVALID_CATEGORY);
        }
    }
}