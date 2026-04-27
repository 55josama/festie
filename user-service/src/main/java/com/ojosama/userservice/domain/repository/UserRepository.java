package com.ojosama.userservice.domain.repository;

import com.ojosama.userservice.domain.model.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
}
