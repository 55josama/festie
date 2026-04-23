package com.ojosama.operationservice.domain.model.entity;

import com.ojosama.common.audit.BaseEntity;
import com.ojosama.operationservice.domain.model.enums.OperationRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_operation_request", schema = "operation_service")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperationRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "requester_id", nullable = false)
    private UUID requesterId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OperationRequestStatus status;

    @Column(name = "admin_memo")
    private String adminMemo;

    @Builder
    private OperationRequest(UUID requesterId, String title, String content) {
        this.requesterId = requesterId;
        this.title = title;
        this.content = content;
        this.status = OperationRequestStatus.PENDING;
    }

    public static OperationRequest of(UUID requesterId, String title, String content) {
        return OperationRequest.builder()
                .requesterId(requesterId)
                .title(title)
                .content(content)
                .build();
    }

    public void resolve(String adminMemo) {
        this.status = OperationRequestStatus.RESOLVED;
        this.adminMemo = adminMemo;
    }

    public void reject(String adminMemo) {
        this.status = OperationRequestStatus.REJECTED;
        this.adminMemo = adminMemo;
    }
}
