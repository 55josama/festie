package com.ojosama.notificationservice.infrastructure.persistence;

import com.ojosama.notificationservice.domain.model.emailLog.EmailLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEmailLogRepository extends JpaRepository<EmailLog, UUID> {
}
