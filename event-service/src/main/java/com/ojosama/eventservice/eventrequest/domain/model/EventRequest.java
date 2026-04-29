package com.ojosama.eventservice.eventrequest.domain.model;

import com.ojosama.common.audit.BaseUserEntity;
import com.ojosama.eventservice.event.domain.model.EventCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_event_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventRequest extends BaseUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "requester_id", nullable = false)
    private UUID requesterId;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private EventCategory category;

    @Column(name = "link", length = 500, nullable = false)
    private String link;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "reject_reason", length = 300)
    private String rejectReason;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventRequestStatus status;

    public static EventRequest create(UUID requesterId, String eventName, EventCategory category,
                                      String link, String description) {
        EventRequest request = new EventRequest();
        request.requesterId = requesterId;
        request.eventName = eventName;
        request.category = category;
        request.link = link;
        request.description = description;
        request.status = EventRequestStatus.PENDING;
        return request;
    }

    public void cancel(UUID userId) {
        // TODO: 취소 로직 미구현
    }
}
