package com.ojosama.eventservice.event.domain.model;

import com.ojosama.common.audit.BaseEntity;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.model.vo.EventFee;
import com.ojosama.eventservice.event.domain.model.vo.EventLocation;
import com.ojosama.eventservice.event.domain.model.vo.EventTicketing;
import com.ojosama.eventservice.event.domain.model.vo.EventTime;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private EventCategory category;

    @Embedded
    private EventTime eventTime;

    @Embedded
    private EventLocation eventLocation;

    @Embedded
    private EventFee eventFee;

    @Embedded
    private EventTicketing eventTicketing;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Column(name = "official_link", length = 500)
    private String officialLink;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "performer", length = 500)
    private String performer;

    @Column(name = "img", length = 500, nullable = false)
    private String img;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EventSchedule> schedules = new ArrayList<>();

    @Builder
    public Event(String name, EventCategory category, EventTime eventTime,
                 EventLocation eventLocation, EventFee eventFee, EventTicketing eventTicketing,
                 EventStatus status, String description, String performer, String img,
                 String officialLink) {
        validateEventName(name);
        validateCategory(category);
        validateDescription(description);
        validatePerformer(performer);
        validateImg(img);
        validateOfficialLink(officialLink);

        this.name = name;
        this.category = category;
        this.eventTime = eventTime;
        this.eventLocation = eventLocation;
        this.eventFee = eventFee;
        this.eventTicketing = eventTicketing;
        this.status = status != null ? status : EventStatus.SCHEDULED;
        this.description = description;
        this.performer = performer;
        this.img = img;
        this.officialLink = officialLink;
        this.schedules = new ArrayList<>();
    }

    private void validateEventName(String name) {
        if (name == null || name.isBlank()) {
            throw new EventException(EventErrorCode.EVENT_INVALID_NAME);
        }
        if (name.length() > 100) {
            throw new EventException(EventErrorCode.EVENT_INVALID_NAME);
        }
    }

    private void validateCategory(EventCategory category) {
        if (category == null) {
            throw new EventException(EventErrorCode.EVENT_CATEGORY_INVALID);
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new EventException(EventErrorCode.EVENT_INVALID_DESCRIPTION);
        }
    }

    private void validatePerformer(String performer) {
        if (performer != null && performer.length() > 500) {
            throw new EventException(EventErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateImg(String img) {
        if (img == null || img.isBlank()) {
            throw new EventException(EventErrorCode.EVENT_INVALID_IMAGE);
        }
        if (img.length() > 500) {
            throw new EventException(EventErrorCode.EVENT_INVALID_IMAGE);
        }
    }

    private void validateOfficialLink(String officialLink) {
        if (officialLink != null && officialLink.length() > 500) {
            throw new EventException(EventErrorCode.VALIDATION_ERROR);
        }
    }

    public void addSchedule(EventSchedule schedule) {
        if (schedule == null) {
            throw new EventException(EventErrorCode.EVENT_SCHEDULE_INVALID_TIME);
        }
        this.schedules.add(schedule);
    }

    public void removeSchedule(EventSchedule schedule) {
        this.schedules.remove(schedule);
    }
}
