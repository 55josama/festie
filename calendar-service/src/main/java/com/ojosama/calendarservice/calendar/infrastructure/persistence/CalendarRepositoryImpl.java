package com.ojosama.calendarservice.calendar.infrastructure.persistence;

import com.ojosama.calendarservice.calendar.domain.model.Calendar;
import com.ojosama.calendarservice.calendar.domain.repository.CalendarRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CalendarRepositoryImpl implements CalendarRepository {

    private final JpaCalendarRepository jpaCalendarRepository;

    @Override
    public Calendar save(Calendar calendar) {
        return jpaCalendarRepository.save(calendar);
    }

    @Override
    public Optional<Calendar> findByIdAndDeletedAtIsNull(UUID id) {
        return jpaCalendarRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public Page<Calendar> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable) {
        return jpaCalendarRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
    }

    @Override
    public Page<Calendar> findByUserIdAndEventDateBetweenAndDeletedAtIsNull(
            UUID userId, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return jpaCalendarRepository.findByUserIdAndEventDateBetweenAndDeletedAtIsNull(userId, start, end, pageable);
    }
}
