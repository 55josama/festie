package com.ojosama.userservice.domain.repository;

import com.ojosama.userservice.domain.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Page<User> findAllByDeletedAtIsNull(Pageable pageable);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
