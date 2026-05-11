package com.ojosama.notificationservice.domain.repository;

import com.ojosama.notificationservice.domain.model.notification.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepository {

    Long deleteOldNotifications(LocalDateTime time);

    List<Notification> findByReceiverIdAndReadAtIsNullAndDeletedAtIsNull(UUID receiverId);

    Page<Notification> findByReceiverIdAndDeletedAtIsNull(UUID receiverId, Pageable pageable);

    Notification save(Notification notification);

    void saveAll(List<Notification> notifications);

    Optional<Notification> findByIdAndReceiverIdAndDeletedAtIsNull(UUID notificationId, UUID receiverId);
}
