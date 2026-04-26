package com.ojosama.eventservice.event.domain.model;

import com.ojosama.common.audit.BaseUserEntity;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_event_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventCategory extends BaseUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    @Builder(access = AccessLevel.PRIVATE)
    private EventCategory(String name) {
        validateCategoryName(name);
        this.name = name.trim();
    }

    public static EventCategory create(String name) {
        return EventCategory.builder().name(name).build();
    }

    public void update(String name) {
        validateCategoryName(name);
        this.name = name.trim();
    }

    private void validateCategoryName(String name) {
        if (name == null || name.isBlank()) {
            throw new EventException(EventErrorCode.EVENT_CATEGORY_INVALID);
        }
        if (name.trim().length() > 50) {
            throw new EventException(EventErrorCode.EVENT_CATEGORY_INVALID);
        }
    }
}
