package com.ojosama.notificationservice.domain.model.notification;

import com.ojosama.common.audit.BaseEntity;
import com.ojosama.notificationservice.domain.model.emailLog.EmailLog;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "p_notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @Column(name = "id")
    @UuidGenerator
    @GeneratedValue
    private UUID id;

    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Embedded
    private TargetInfo targetInfo;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @OneToMany(mappedBy = "notification")
    private List<EmailLog> emailLogs;

    @Builder
    public Notification(UUID receiverId, String title, String content, TargetInfo targetInfo) {
        this.receiverId = receiverId;
        this.title = title;
        this.content = content;
        this.targetInfo = targetInfo;
    }

    public static Notification of(UUID receiverId, String title, String content, TargetInfo targetInfo) {
        return Notification.builder()
                .receiverId(receiverId)
                .title(title)
                .content(content)
                .targetInfo(targetInfo)
                .build();
    }

    public void readAt() {
        if (this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }

    }
}
