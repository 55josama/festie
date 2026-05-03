package com.ojosama.userservice.domain.model;

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
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
        }

        try {
            return Category.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
        }
    }
}