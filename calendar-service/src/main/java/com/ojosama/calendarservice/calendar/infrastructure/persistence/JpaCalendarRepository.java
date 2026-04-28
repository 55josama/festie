package com.ojosama.calendarservice.calendar.infrastructure.persistence;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCalendarRepository extends JpaRepository<Calendar, UUID>, CalendarRepositoryCustom {

    Optional<Calendar> findByEventInfo_EventScheduleIdAndUserId(UUID eventScheduleId, UUID userId);

    Optional<Calendar> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
}
