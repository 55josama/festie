package com.ojosama.notificationservice.infrastructure.persistence;

import com.ojosama.notificationservice.domain.model.notification.Notification;
import com.ojosama.notificationservice.domain.model.notification.QNotification;
import com.ojosama.notificationservice.domain.repository.NotificationRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final JpaNotificationRepository jpaNotificationRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Notification> findByReceiverIdAndReadAtIsNullAndDeletedAtIsNull(UUID receiverId) {
        return jpaNotificationRepository.findByReceiverIdAndReadAtIsNullAndDeletedAtIsNull(receiverId);
    }

    @Override
    public List<Notification> findByReceiverIdAndDeletedAtIsNull(UUID receiverId) {
        return jpaNotificationRepository.findByReceiverIdAndDeletedAtIsNull(receiverId);
    }

    @Override
    public void save(Notification notification) {
        jpaNotificationRepository.save(notification);
    }

    @Override
    public void saveAll(List<Notification> notifications) {
        jpaNotificationRepository.saveAll(notifications);
    }

    @Override
    public Long deleteOldNotifications(LocalDateTime time) {
        QNotification notification = QNotification.notification;
        return jpaQueryFactory.update(notification)
                .set(notification.deletedAt, LocalDateTime.now())
                .set(notification.deletedBy, UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .where(
                        notification.createdAt.before(time),
                        notification.deletedAt.isNull()
                )
                .execute();
    }

    @Override
    public Optional<Notification> findByIdAndReceiverIdAndDeletedAtIsNull(UUID notificationId, UUID receiverId) {
        return jpaNotificationRepository.findByIdAndReceiverIdAndDeletedAtIsNull(notificationId, receiverId);
    }

}
