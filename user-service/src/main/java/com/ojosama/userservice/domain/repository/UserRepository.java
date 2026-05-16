package com.ojosama.userservice.domain.repository;

import com.ojosama.userservice.domain.model.User;
import com.ojosama.userservice.domain.model.UserRole;
import java.util.List;
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

    @Query("""
            select u
            from User u
            where u.deletedAt is null
              and (:email is null or :email = '' or lower(u.email) like lower(concat('%', :email, '%')))
              and (:name is null or :name = '' or
                   lower(u.name) like lower(concat('%', :name, '%')) or
                   lower(u.nickname) like lower(concat('%', :name, '%'))
                  )
              and (:role is null or u.role = :role)
            """)
    Page<User> findAllByDeletedAtIsNullAndSearch(
            @Param("email") String email,
            @Param("name") String name,
            @Param("role") UserRole role,
            Pageable pageable
    );

    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByNicknameAndDeletedAtIsNull(String nickname);

    boolean existsByNicknameAndIdNotAndDeletedAtIsNull(String nickname, UUID id);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    List<User> findAllByIdInAndDeletedAtIsNull(List<UUID> ids);

    Optional<User> findFirstByRoleAndDeletedAtIsNull(UserRole role);

    @Modifying
    @Query("""
            update User u
            set u.refreshTokenHash = :newRefreshTokenHash
            where u.id = :userId
              and u.refreshTokenHash = :oldRefreshTokenHash
              and u.deletedAt is null
            """)
    int rotateRefreshTokenHash(
            @Param("userId") UUID userId,
            @Param("oldRefreshTokenHash") String oldRefreshTokenHash,
            @Param("newRefreshTokenHash") String newRefreshTokenHash
    );
}
