package com.ojosama.operationrequest.domain.model.entity;

import com.ojosama.common.audit.BaseEntity;
import com.ojosama.operationrequest.domain.exception.OperationRequestErrorCode;
import com.ojosama.operationrequest.domain.exception.OperationRequestException;
import com.ojosama.operationrequest.domain.model.enums.OperationRequestStatus;
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

    @Column(columnDefinition = "TEXT", nullable = false)
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

    public void update(String title, String content) {
        if (this.status != OperationRequestStatus.PENDING) {
            throw new OperationRequestException(OperationRequestErrorCode.INVALID_UPDATE_STATUS);
        }
        this.title = title;
        this.content = content;
    }

    public void updateStatus(OperationRequestStatus newStatus, String adminMemo) {
        if (this.status == OperationRequestStatus.PENDING && newStatus != OperationRequestStatus.PENDING) {
            if (adminMemo == null || adminMemo.isBlank()) {
                throw new OperationRequestException(OperationRequestErrorCode.ADMIN_MEMO_REQUIRED);
            }
        }
        this.status = newStatus;
        if (adminMemo != null) {
            this.adminMemo = adminMemo;
        }
    }

    public void validateDeletableBy(UUID userId) {
        if (!this.requesterId.equals(userId)) {
            throw new OperationRequestException(OperationRequestErrorCode.UNAUTHORIZED_DELETE);
        }
        if (this.status != OperationRequestStatus.PENDING) {
            throw new OperationRequestException(OperationRequestErrorCode.INVALID_DELETE_STATUS);
        }
    }
}
