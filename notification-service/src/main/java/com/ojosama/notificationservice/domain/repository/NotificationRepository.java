package com.ojosama.notificationservice.domain.repository;

import com.ojosama.notificationservice.domain.model.notification.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Long deleteOldNotifications();

    List<Notification> findByReceiverIdAndReadAtIsNullAndDeletedAtIsNull(UUID receiverId);

    List<Notification> findByReceiverIdAndDeletedAtIsNull(UUID receiverId);

    void save(Notification notification);

    void saveAll(List<Notification> notifications);

    Optional<Notification> findByIdAndReceiverIdAndDeletedAtIsNull(UUID notificationId, UUID receiverId);
}
