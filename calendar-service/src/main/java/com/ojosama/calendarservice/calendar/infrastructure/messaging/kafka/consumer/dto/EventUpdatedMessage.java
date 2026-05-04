package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.consumer.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonSerialize(using = LocalDateTimeSerializer.class)
@JsonDeserialize(using = LocalDateTimeDeserializer.class)
public record EventUpdatedMessage(
        UUID eventId,
        String eventName,
        List<FieldChange> changedFields,
        String place,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer minFee,
        Integer maxFee,
        Boolean hasTicketing,
        LocalDateTime ticketingOpenAt,
        LocalDateTime ticketingCloseAt,
        String ticketingLink,
        String status,
        String officialLink,
        String description,
        String performer,
        String img
) {
    public record FieldChange(
            String fieldName,
            String before,
            String after
    ) {
    }
}
