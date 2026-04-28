package com.ojosama.userservice.domain.repository;

import com.ojosama.userservice.domain.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    Page<User> findAllByDeletedAtIsNull(Pageable pageable);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    @Modifying
    @Query("""
            update User u
            set u.refreshToken = :newRefreshToken
            where u.id = :userId
              and u.refreshToken = :oldRefreshToken
              and u.deletedAt is null
            """)
    int rotateRefreshToken(
            @Param("userId") UUID userId,
            @Param("oldRefreshToken") String oldRefreshToken,
            @Param("newRefreshToken") String newRefreshToken
    );
}