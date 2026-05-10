package com.ojosama.notificationservice.infrastructure.persistence;

import com.ojosama.notificationservice.domain.model.notification.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaNotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByReceiverIdAndReadAtIsNullAndDeletedAtIsNull(UUID receiverId);

    Page<Notification> findByReceiverIdAndDeletedAtIsNull(UUID receiverId, Pageable pageable);

    Optional<Notification> findByIdAndReceiverIdAndDeletedAtIsNull(UUID notificationId, UUID receiverId);
}
