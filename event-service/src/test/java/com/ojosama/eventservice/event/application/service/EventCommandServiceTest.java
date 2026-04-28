package com.ojosama.eventservice.event.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ojosama.eventservice.event.application.dto.command.CreateEventCommand;
import com.ojosama.eventservice.event.application.dto.command.CreateScheduleCommand;
import com.ojosama.eventservice.event.application.dto.command.UpdateEventCommand;
import com.ojosama.eventservice.event.application.dto.result.EventResult;
import com.ojosama.eventservice.event.domain.event.EventMessagePublisher;
import com.ojosama.eventservice.event.domain.event.payload.EventCreatedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventDeletedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventScheduleChangedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventUpdatedMessage;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.model.Event;
import com.ojosama.eventservice.event.domain.model.EventCategory;
import com.ojosama.eventservice.event.domain.model.EventStatus;
import com.ojosama.eventservice.event.domain.model.vo.EventFee;
import com.ojosama.eventservice.event.domain.model.vo.EventLocation;
import com.ojosama.eventservice.event.domain.model.vo.EventTicketing;
import com.ojosama.eventservice.event.domain.model.vo.EventTime;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventCommandService 단위 테스트")
class EventCommandServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventCategoryRepository eventCategoryRepository;
    @Mock
    private EventMessagePublisher eventMessagePublisher;
    @InjectMocks
    private EventCommandServiceImpl eventService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final LocalDateTime FUTURE_START = LocalDateTime.now().plusDays(30);
    private static final LocalDateTime FUTURE_END = LocalDateTime.now().plusDays(60);

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private CreateEventCommand buildCommand(UUID categoryId, boolean hasTicketing) {
        List<CreateScheduleCommand> schedules = List.of(
                new CreateScheduleCommand("메인 공연", FUTURE_START, FUTURE_START.plusHours(2)));
        if (hasTicketing) {
            return new CreateEventCommand(
                    USER_ID, "서울 재즈 페스티벌", categoryId,
                    FUTURE_START, FUTURE_END,
                    "올림픽공원", new BigDecimal("37.52"), new BigDecimal("127.12"),
                    10000, 50000, true,
                    FUTURE_START.minusDays(10), FUTURE_START.minusDays(1),
                    "http://ticket.example.com", "http://official.example.com",
                    "최고의 재즈 페스티벌", "재즈밴드A", "http://img.example.com/banner.jpg",
                    schedules);
        }
        return new CreateEventCommand(
                USER_ID, "서울 재즈 페스티벌", categoryId,
                FUTURE_START, FUTURE_END,
                "올림픽공원", new BigDecimal("37.52"), new BigDecimal("127.12"),
                0, 50000, false, null, null, null,
                "http://official.example.com",
                "최고의 재즈 페스티벌", "재즈밴드A", "http://img.example.com/banner.jpg",
                schedules);
    }

    /**
     * 단위 테스트용 Event 생성 (id는 ReflectionTestUtils로 주입). JPA @GeneratedValue는 영속 시점에 채워지므로 직접 설정 필요.
     */
    private Event createDefaultEvent() {
        EventCategory category = EventCategory.create("CONCERT");
        Event event = Event.builder()
                .name("서울 재즈 페스티벌")
                .category(category)
                .eventTime(new EventTime(FUTURE_START, FUTURE_END))
                .eventLocation(new EventLocation("올림픽공원", new BigDecimal("37.52"), new BigDecimal("127.12")))
                .eventFee(new EventFee(10000, 50000))
                .eventTicketing(new EventTicketing(false, null, null, null))
                .description("최고의 재즈 페스티벌")
                .performer("재즈밴드A")
                .img("http://img.example.com/banner.jpg")
                .officialLink("http://official.example.com")
                .build();
        ReflectionTestUtils.setField(event, "id", EVENT_ID);
        return event;
    }

    private UpdateEventCommand buildUpdateCommand(
            UUID eventId, String name,
            LocalDateTime startAt, LocalDateTime endAt,
            String place, Integer minFee, Integer maxFee,
            Boolean hasTicketing,
            LocalDateTime ticketingOpenAt, LocalDateTime ticketingCloseAt, String ticketingLink,
            List<CreateScheduleCommand> schedules) {
        return new UpdateEventCommand(
                eventId, USER_ID, name, CATEGORY_ID,
                startAt, endAt,
                place, new BigDecimal("37.52"), new BigDecimal("127.12"),
                minFee, maxFee,
                hasTicketing, ticketingOpenAt, ticketingCloseAt, ticketingLink,
                "http://official.example.com",
                "최고의 재즈 페스티벌", "재즈밴드A", "http://img.example.com/banner.jpg",
                schedules);
    }

    // ─── 행사 등록 ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("행사 등록 실패")
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
                    0, 50000, false, null, null, null, null,
                    "설명", null, "http://img.example.com/banner.jpg",
                    List.of(new CreateScheduleCommand("메인 공연", FUTURE_START, FUTURE_START.plusHours(2))));

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
                    10000, 50000, true,
                    FUTURE_START.minusDays(10), FUTURE_START.minusDays(1),
                    null, null, "설명", null, "http://img.example.com/banner.jpg",
                    List.of(new CreateScheduleCommand("메인 공연", FUTURE_START, FUTURE_START.plusHours(2))));

            assertThatThrownBy(() -> eventService.createEvent(command))
                    .isInstanceOf(EventException.class)
                    .hasMessageContaining(EventErrorCode.TICKETING_NOT_AVAILABLE.getMessage());

            verify(eventRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("행사 등록 성공")
    class CreateEventSuccess {

        @Test
        @DisplayName("has_ticketing=false → status=SCHEDULED, 일정 저장, event.info.created 발행")
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
            verify(eventMessagePublisher).publishEventCreated(any());
        }

        @Test
        @DisplayName("has_ticketing=true → ticketing 정보 저장, event.info.created 발행")
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
            verify(eventMessagePublisher).publishEventCreated(any());
        }

        @Test
        @DisplayName("event.info.created 메시지에 categoryName, startAt, endAt 포함")
        void createEvent_publishesCreatedMessageWithCorrectPayload() {
            EventCategory category = EventCategory.create("FESTIVAL");
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(category));
            given(eventRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            eventService.createEvent(buildCommand(CATEGORY_ID, false));

            ArgumentCaptor<EventCreatedMessage> captor = ArgumentCaptor.forClass(EventCreatedMessage.class);
            verify(eventMessagePublisher).publishEventCreated(captor.capture());

            EventCreatedMessage message = captor.getValue();
            assertThat(message.categoryName()).isEqualTo("FESTIVAL");
            assertThat(message.eventStartAt()).isEqualTo(FUTURE_START);
            assertThat(message.eventEndAt()).isEqualTo(FUTURE_END);
        }
    }

    // ─── 행사 수정 ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("행사 수정 실패")
    class UpdateEventFailure {

        @Test
        @DisplayName("존재하지 않는 eventId → EVENT_NOT_FOUND 예외")
        void updateEvent_unknownEventId_throwsException() {
            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.empty());

            UpdateEventCommand command = buildUpdateCommand(
                    EVENT_ID, "새 이름",
                    FUTURE_START, FUTURE_END, "올림픽공원",
                    10000, 50000, false, null, null, null, null);

            assertThatThrownBy(() -> eventService.updateEvent(command))
                    .isInstanceOf(EventException.class)
                    .hasMessageContaining(EventErrorCode.EVENT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("존재하지 않는 categoryId → EVENT_CATEGORY_NOT_FOUND 예외")
        void updateEvent_unknownCategoryId_throwsException() {
            Event event = createDefaultEvent();
            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.empty());

            UpdateEventCommand command = buildUpdateCommand(
                    EVENT_ID, "새 이름",
                    FUTURE_START, FUTURE_END, "올림픽공원",
                    10000, 50000, false, null, null, null, null);

            assertThatThrownBy(() -> eventService.updateEvent(command))
                    .isInstanceOf(EventException.class)
                    .hasMessageContaining(EventErrorCode.EVENT_CATEGORY_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("행사 수정 성공")
    class UpdateEventSuccess {

        @Test
        @DisplayName("이름 변경 → publishEventUpdated에 변경된 eventId, eventName 포함")
        void updateEvent_nameChanged_publishEventUpdatedWithCorrectPayload() {
            Event event = createDefaultEvent();
            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(event.getCategory()));

            UpdateEventCommand command = buildUpdateCommand(
                    EVENT_ID, "변경된 이름",
                    FUTURE_START, FUTURE_END, "올림픽공원",
                    10000, 50000, false, null, null, null, null);

            eventService.updateEvent(command);

            ArgumentCaptor<EventUpdatedMessage> captor = ArgumentCaptor.forClass(EventUpdatedMessage.class);
            verify(eventMessagePublisher).publishEventUpdated(captor.capture());
            assertThat(captor.getValue().eventId()).isEqualTo(EVENT_ID);
            assertThat(captor.getValue().eventName()).isEqualTo("변경된 이름");
        }

        @Test
        @DisplayName("필드 변경 시 → publishScheduleChanged에 변경된 fieldName 목록 포함")
        void updateEvent_fieldChanged_publishScheduleChangedWithChangedFields() {
            Event event = createDefaultEvent();
            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(event.getCategory()));

            UpdateEventCommand command = buildUpdateCommand(
                    EVENT_ID, "변경된 이름",
                    FUTURE_START, FUTURE_END, "올림픽공원",
                    10000, 50000, false, null, null, null, null);

            eventService.updateEvent(command);

            ArgumentCaptor<EventScheduleChangedMessage> captor =
                    ArgumentCaptor.forClass(EventScheduleChangedMessage.class);
            verify(eventMessagePublisher).publishScheduleChanged(captor.capture());

            EventScheduleChangedMessage message = captor.getValue();
            assertThat(message.eventId()).isEqualTo(EVENT_ID);
            assertThat(message.changedFields())
                    .extracting("fieldName")
                    .contains("name");
        }

        @Test
        @DisplayName("변경 없이 동일 값 전달 → publishEventUpdated만 발행, publishScheduleChanged 미발행")
        void updateEvent_noFieldChanges_onlyPublishEventUpdated() {
            Event event = createDefaultEvent();
            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(event.getCategory()));

            // 기존 값과 동일하게 전달
            UpdateEventCommand command = buildUpdateCommand(
                    EVENT_ID, event.getName(),
                    event.getEventTime().getStartAt(), event.getEventTime().getEndAt(),
                    event.getEventLocation().getPlace(),
                    event.getEventFee().getMinFee(), event.getEventFee().getMaxFee(),
                    event.getEventTicketing().getHasTicketing(),
                    null, null, null, null);

            eventService.updateEvent(command);

            verify(eventMessagePublisher).publishEventUpdated(any());
            verify(eventMessagePublisher, never()).publishScheduleChanged(any());
        }

        @Test
        @DisplayName("schedules 포함 시 → 기존 일정 교체, 새 일정 2개로 대체")
        void updateEvent_withSchedules_schedulesReplaced() {
            Event event = createDefaultEvent();
            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(event.getCategory()));

            List<CreateScheduleCommand> newSchedules = List.of(
                    new CreateScheduleCommand("Day 1", FUTURE_START, FUTURE_START.plusHours(3)),
                    new CreateScheduleCommand("Day 2", FUTURE_START.plusDays(1),
                            FUTURE_START.plusDays(1).plusHours(3)));

            UpdateEventCommand command = buildUpdateCommand(
                    EVENT_ID, event.getName(),
                    event.getEventTime().getStartAt(), event.getEventTime().getEndAt(),
                    event.getEventLocation().getPlace(),
                    event.getEventFee().getMinFee(), event.getEventFee().getMaxFee(),
                    event.getEventTicketing().getHasTicketing(),
                    null, null, null, newSchedules);

            EventResult result = eventService.updateEvent(command);

            assertThat(result.schedules()).hasSize(2);
            assertThat(result.schedules())
                    .extracting("name")
                    .containsExactly("Day 1", "Day 2");
        }

        @Test
        @DisplayName("schedules 미포함(null) 시 → 기존 일정 유지")
        void updateEvent_withoutSchedules_schedulesPreserved() {
            Event event = createDefaultEvent();
            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(event.getCategory()));

            UpdateEventCommand command = buildUpdateCommand(
                    EVENT_ID, event.getName(),
                    event.getEventTime().getStartAt(), event.getEventTime().getEndAt(),
                    event.getEventLocation().getPlace(),
                    event.getEventFee().getMinFee(), event.getEventFee().getMaxFee(),
                    event.getEventTicketing().getHasTicketing(),
                    null, null, null, null);  // schedules = null

            EventResult result = eventService.updateEvent(command);

            assertThat(result.schedules()).isEmpty();
        }

        @Test
        @DisplayName("여러 필드 동시 변경 → publishScheduleChanged 메시지에 변경된 모든 fieldName 포함")
        void updateEvent_multipleFieldsChanged_allChangedFieldsInMessage() {
            Event event = createDefaultEvent();
            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(event.getCategory()));

            UpdateEventCommand command = buildUpdateCommand(
                    EVENT_ID, "새 이름",
                    FUTURE_START, FUTURE_END, "잠실종합운동장",  // place 변경
                    0, 30000,                                      // maxFee 변경
                    false, null, null, null, null);

            eventService.updateEvent(command);

            ArgumentCaptor<EventScheduleChangedMessage> captor =
                    ArgumentCaptor.forClass(EventScheduleChangedMessage.class);
            verify(eventMessagePublisher).publishScheduleChanged(captor.capture());

            assertThat(captor.getValue().changedFields())
                    .extracting("fieldName")
                    .contains("name", "place", "maxFee");
        }
    }

    // ─── 행사 삭제 ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("행사 삭제 실패")
    class DeleteEventFailure {

        @Test
        @DisplayName("존재하지 않는 eventId → EVENT_NOT_FOUND 예외, 메시지 미발행")
        void deleteEvent_unknownEventId_throwsException() {
            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.deleteEvent(USER_ID, EVENT_ID))
                    .isInstanceOf(EventException.class)
                    .hasMessageContaining(EventErrorCode.EVENT_NOT_FOUND.getMessage());

            verify(eventMessagePublisher, never()).publishEventDeleted(any());
        }
    }

    @Nested
    @DisplayName("행사 삭제 성공")
    class DeleteEventSuccess {

        @Test
        @DisplayName("유효한 eventId → soft delete 후 event.info.deleted 발행")
        void deleteEvent_validEventId_publishEventDeleted() {
            Event event = createDefaultEvent();
            given(eventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));

            eventService.deleteEvent(USER_ID, EVENT_ID);

            verify(eventRepository).delete(event);

            ArgumentCaptor<EventDeletedMessage> captor = ArgumentCaptor.forClass(EventDeletedMessage.class);
            verify(eventMessagePublisher).publishEventDeleted(captor.capture());
            assertThat(captor.getValue().eventId()).isEqualTo(EVENT_ID);
            assertThat(captor.getValue().eventName()).isEqualTo("서울 재즈 페스티벌");
        }
    }
}
