package com.ojosama.notificationservice.domain.model.emailLog;

import com.ojosama.common.audit.BaseEntity;
import com.ojosama.notificationservice.domain.model.notification.Notification;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Table(name = "p_email_log")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailLog extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false)
    @UuidGenerator
    @GeneratedValue
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "status", nullable = false)
    private Status status;
}
