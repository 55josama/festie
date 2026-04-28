package com.ojosama.calendarservice.calendar.infrastructure.persistence;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCalendarRepository extends JpaRepository<Calendar, UUID> {

    Optional<Calendar> findByIdAndDeletedAtIsNull(UUID id);

    Optional<Calendar> findByEventScheduleIdAndUserId(UUID eventScheduleId, UUID userId);
}
