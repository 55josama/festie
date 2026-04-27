package com.ojosama.eventservice.event.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ojosama.eventservice.event.application.dto.command.CreateEventCommand;
import com.ojosama.eventservice.event.application.dto.command.CreateScheduleCommand;
import com.ojosama.eventservice.event.application.dto.result.EventResult;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.model.EventCategory;
import com.ojosama.eventservice.event.domain.model.EventStatus;
import com.ojosama.eventservice.event.domain.repository.EventCategoryRepository;
import com.ojosama.eventservice.event.domain.repository.EventRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService 단위 테스트")
class EventCommandServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventCategoryRepository eventCategoryRepository;

    @InjectMocks
    private EventCommandServiceImpl eventService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();

    private static final LocalDateTime FUTURE_START = LocalDateTime.now().plusDays(30);
    private static final LocalDateTime FUTURE_END = LocalDateTime.now().plusDays(60);

    private CreateEventCommand buildCommand(UUID categoryId, boolean hasTicketing) {
        List<CreateScheduleCommand> schedules = List.of(
                new CreateScheduleCommand("메인 공연", FUTURE_START, FUTURE_START.plusHours(2))
        );

        if (hasTicketing) {
            return new CreateEventCommand(
                    USER_ID, "서울 재즈 페스티벌", categoryId,
                    FUTURE_START, FUTURE_END,
                    "올림픽공원", new BigDecimal("37.52"), new BigDecimal("127.12"),
                    10000, 50000,
                    true,
                    FUTURE_START.minusDays(10), FUTURE_START.minusDays(1),
                    "http://ticket.example.com",
                    "http://official.example.com",
                    "최고의 재즈 페스티벌", "재즈밴드A", "http://img.example.com/banner.jpg",
                    schedules
            );
        }

        return new CreateEventCommand(
                USER_ID, "서울 재즈 페스티벌", categoryId,
                FUTURE_START, FUTURE_END,
                "올림픽공원", new BigDecimal("37.52"), new BigDecimal("127.12"),
                0, 50000,
                false, null, null, null,
                "http://official.example.com",
                "최고의 재즈 페스티벌", "재즈밴드A", "http://img.example.com/banner.jpg",
                schedules
        );
    }

    @Nested
    @DisplayName("행사 등록 실패 (Red)")
    class CreateEventFailure {

        @Test
        @DisplayName("존재하지 않는 category_id → EVENT_CATEGORY_NOT_FOUND 예외")
        void createEvent_unknownCategoryId_throwsException() {
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.createEvent(buildCommand(CATEGORY_ID, false)))
                    .isInstanceOf(EventException.class)
                    .hasMessageContaining(EventErrorCode.EVENT_CATEGORY_NOT_FOUND.getMessage());

            verify(eventRepository, never()).save(any());
        }

        @Test
        @DisplayName("start_at > end_at → EVENT_INVALID_TIME 예외 (도메인 검증)")
        void createEvent_startAfterEnd_throwsException() {
            EventCategory category = EventCategory.create("FESTIVAL");
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(category));

            CreateEventCommand command = new CreateEventCommand(
                    USER_ID, "서울 재즈 페스티벌", CATEGORY_ID,
                    FUTURE_END, FUTURE_START,
                    "올림픽공원", new BigDecimal("37.52"), new BigDecimal("127.12"),
                    0, 50000,
                    false, null, null, null, null,
                    "설명", null, "http://img.example.com/banner.jpg",
                    List.of(new CreateScheduleCommand("메인 공연", FUTURE_START, FUTURE_START.plusHours(2)))
            );

            assertThatThrownBy(() -> eventService.createEvent(command))
                    .isInstanceOf(EventException.class)
                    .hasMessageContaining(EventErrorCode.EVENT_INVALID_TIME.getMessage());

            verify(eventRepository, never()).save(any());
        }

        @Test
        @DisplayName("has_ticketing=true + ticketing_link 누락 → TICKETING_NOT_AVAILABLE 예외 (도메인 검증)")
        void createEvent_ticketingTrueWithoutLink_throwsException() {
            EventCategory category = EventCategory.create("CONCERT");
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(category));

            CreateEventCommand command = new CreateEventCommand(
                    USER_ID, "서울 재즈 페스티벌", CATEGORY_ID,
                    FUTURE_START, FUTURE_END,
                    "올림픽공원", new BigDecimal("37.52"), new BigDecimal("127.12"),
                    10000, 50000,
                    true,
                    FUTURE_START.minusDays(10), FUTURE_START.minusDays(1),
                    null,
                    null, "설명", null, "http://img.example.com/banner.jpg",
                    List.of(new CreateScheduleCommand("메인 공연", FUTURE_START, FUTURE_START.plusHours(2)))
            );

            assertThatThrownBy(() -> eventService.createEvent(command))
                    .isInstanceOf(EventException.class)
                    .hasMessageContaining(EventErrorCode.TICKETING_NOT_AVAILABLE.getMessage());

            verify(eventRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("행사 등록 성공 (Green)")
    class CreateEventSuccess {

        @Test
        @DisplayName("has_ticketing=false + 유효 요청 → status=SCHEDULED, 일정 포함 저장")
        void createEvent_withoutTicketing_returnsScheduledStatus() {
            EventCategory category = EventCategory.create("FESTIVAL");
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(category));
            given(eventRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            EventResult result = eventService.createEvent(buildCommand(CATEGORY_ID, false));

            assertThat(result.status()).isEqualTo(EventStatus.SCHEDULED.name());
            assertThat(result.hasTicketing()).isFalse();
            assertThat(result.name()).isEqualTo("서울 재즈 페스티벌");
            assertThat(result.schedules()).hasSize(1);
            verify(eventRepository).save(any());
        }

        @Test
        @DisplayName("has_ticketing=true + ticketing 필드 모두 포함 → status=SCHEDULED, ticketing 정보 저장")
        void createEvent_withTicketing_savedSuccessfully() {
            EventCategory category = EventCategory.create("CONCERT");
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(category));
            given(eventRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            EventResult result = eventService.createEvent(buildCommand(CATEGORY_ID, true));

            assertThat(result.status()).isEqualTo(EventStatus.SCHEDULED.name());
            assertThat(result.hasTicketing()).isTrue();
            assertThat(result.ticketingLink()).isEqualTo("http://ticket.example.com");
            assertThat(result.schedules()).hasSize(1);
            verify(eventRepository).save(any());
        }
    }
}
