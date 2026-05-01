package com.ojosama.category.repository;

import com.ojosama.category.domain.model.Category;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByNameAndDeletedAtIsNull(String name);
}
