package org.festie.user.domain.repository;

import java.util.UUID;
import org.festie.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
}
