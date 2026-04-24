package com.ojosama.eventservice.event.domain.model;

import com.ojosama.common.audit.BaseEntity;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
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

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "place", length = 500, nullable = false)
    private String place;

    @Column(name = "latitude", precision = 10, scale = 2, nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 2, nullable = false)
    private BigDecimal longitude;

    @Column(name = "min_fee", nullable = false)
    private Integer minFee;

    @Column(name = "max_fee", nullable = false)
    private Integer maxFee;

    @Column(name = "has_ticketing", nullable = false)
    private Boolean hasTicketing;

    @Column(name = "ticketing_open_at")
    private LocalDateTime ticketingOpenAt;

    @Column(name = "ticketing_close_at")
    private LocalDateTime ticketingCloseAt;

    @Column(name = "ticketing_link", length = 500)
    private String ticketingLink;

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
}
