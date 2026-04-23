package com.ojosama.favoriteservice.domain.model;

import com.ojosama.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Event extends BaseEntity {

    @Column(name = "event_id", nullable = false)
    private UUID id;

    @Column(name = "category", nullable = false)
    private String category;
}
