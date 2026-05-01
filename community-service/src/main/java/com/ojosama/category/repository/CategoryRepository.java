package com.ojosama.category.repository;

import com.ojosama.category.domain.model.Category;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByNameAndDeletedAtIsNull(String name);

    Optional<Category> findByIdAndDeletedAtIsNull(UUID id);

    Page<Category> findByDeletedAtIsNull(Pageable pageable);
 }
