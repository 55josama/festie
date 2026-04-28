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
    public List<Notification> findByReceiverIdAndReadAtIsNull(UUID receiverId) {
        return jpaNotificationRepository.findByReceiverIdAndReadAtIsNull(receiverId);
    }

    @Override
    public List<Notification> findByReceiverIdAndDeletedAtIsNull(UUID receiverId) {
        return jpaNotificationRepository.findByReceiverIdAndReadAtIsNullAndDeletedAtIsNull(receiverId);
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
    public Long deleteOldNotifications() {
        QNotification notification = QNotification.notification;
        return jpaQueryFactory.update(notification)
                .set(notification.deletedAt, LocalDateTime.now())
                // TODO : 설정 추가 되면 주석 삭제 예정
                //.set(notification.deletedBy, "SYSTEM")
                .where(
                        notification.createdAt.before(LocalDateTime.now().minusDays(15)),
                        notification.deletedAt.isNull()
                )
                .execute();
    }

    @Override
    public Optional<Notification> findByIdAndReceiverId(UUID notificationId, UUID receiverId) {
        return jpaNotificationRepository.findByIdAndReceiverId(notificationId, receiverId);
    }

}
