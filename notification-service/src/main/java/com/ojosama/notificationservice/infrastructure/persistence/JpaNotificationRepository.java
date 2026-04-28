package com.ojosama.notificationservice.infrastructure.persistence;

import com.ojosama.notificationservice.domain.model.notification.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaNotificationRepository extends JpaRepository<Notification, UUID> {

    int deleteOldNotifications(LocalDateTime time);

    Optional<Notification> findByIdAndReceiverId(UUID notificationId, UUID userId);

    List<Notification> findByReceiverIdAndReadAtIsNull(UUID receiverId);

    List<Notification> findByReceiverIdAndReadAtIsNullAndDeletedAtIsNull(UUID receiverId);
}
