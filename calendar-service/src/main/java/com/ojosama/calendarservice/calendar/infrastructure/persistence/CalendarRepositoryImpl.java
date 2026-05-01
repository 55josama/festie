package com.ojosama.calendarservice.calendar.infrastructure.persistence;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.repository.CalendarRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CalendarRepositoryImpl implements CalendarRepository {

    private final JpaCalendarRepository jpaCalendarRepository;

    @Override
    public void save(Calendar calendar) {
        jpaCalendarRepository.save(calendar);
    }

    @Override
    public Optional<Calendar> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId) {
        return jpaCalendarRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId);
    }

    @Override
    public Optional<Calendar> findByEventInfo_EventScheduleIdAndUserIdAndDeletedAtIsNull(UUID scheduleId, UUID userId) {
        return jpaCalendarRepository.findByEventInfo_EventScheduleIdAndUserIdAndDeletedAtIsNull(scheduleId, userId);
    }

    @Override
    public List<Calendar> findByUserIdAndYearMonth(UUID userId, int year, int month) {
        return jpaCalendarRepository.findByUserIdAndYearMonth(userId, year, month);
    }

    @Override
    public List<Calendar> findAllByEventInfo_EventIdAndDeletedAtIsNull(UUID eventId) {
        return jpaCalendarRepository.findAllByEventInfo_EventIdAndDeletedAtIsNull(eventId);
    }
}
