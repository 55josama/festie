package com.ojosama.favoriteservice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Event {

    @Column(name = "event_id", nullable = false)
    private UUID id;

    @Column(name = "category", nullable = false)
    private String category;
}
