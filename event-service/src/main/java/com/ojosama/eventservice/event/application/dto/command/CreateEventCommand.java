package com.ojosama.eventservice.event.application.dto.command;

import com.ojosama.eventservice.event.domain.model.Event;
import com.ojosama.eventservice.event.domain.model.EventCategory;
import com.ojosama.eventservice.event.domain.model.EventStatus;
import com.ojosama.eventservice.event.domain.model.vo.EventFee;
import com.ojosama.eventservice.event.domain.model.vo.EventLocation;
import com.ojosama.eventservice.event.domain.model.vo.EventTicketing;
import com.ojosama.eventservice.event.domain.model.vo.EventTime;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateEventCommand(
    UUID userId,
    String name,
    UUID categoryId,
    LocalDateTime startAt,
    LocalDateTime endAt,
    String place,
    BigDecimal latitude,
    BigDecimal longitude,
    Integer minFee,
    Integer maxFee,
    Boolean hasTicketing,
    LocalDateTime ticketingOpenAt,
    LocalDateTime ticketingCloseAt,
    String ticketingLink,
    String officialLink,
    String description,
    String performer,
    String img,
    List<CreateScheduleCommand> schedules
) {
    public Event toEntity(EventCategory category) {
        return Event.builder()
            .name(name)
            .category(category)
            .eventTime(new EventTime(startAt, endAt))
            .eventLocation(new EventLocation(place, latitude, longitude))
            .eventFee(new EventFee(minFee, maxFee))
            .eventTicketing(new EventTicketing(hasTicketing, ticketingOpenAt, ticketingCloseAt, ticketingLink))
            .officialLink(officialLink)
            .description(description)
            .performer(performer)
            .img(img)
            .status(EventStatus.SCHEDULED)
            .build();
    }
}
