package com.ojosama.category.domain.model;

import com.ojosama.category.domain.exception.CategoryErrorCode;
import com.ojosama.category.domain.exception.CategoryException;
import com.ojosama.common.audit.BaseEntity;
import com.ojosama.common.audit.BaseUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_community_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseUserEntity {

    private static final int MAX_NAME_LENGTH = 50;

    @Id
    private UUID id;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Builder
    public Category(UUID id, String name) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        String normalized = Objects.requireNonNull(name, "name must not be null").trim();
        if (normalized.isEmpty() || normalized.length() > MAX_NAME_LENGTH) {
            throw new CategoryException(CategoryErrorCode.INVALID_INPUT_VALUE);
        }
        this.name = normalized;
    }

    public void changeName(String name) {
        this.name = normalize(name);
    }

    private static String normalize(String name) {
        Objects.requireNonNull(name, "name must not be null");
        String trimmed = name.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_NAME_LENGTH) {
            throw new CategoryException(CategoryErrorCode.INVALID_INPUT_VALUE);
        }
        return trimmed;
    }

}
