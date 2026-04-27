package com.ojosama.eventservice.event.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ojosama.common.exception.GlobalExceptionHandler;
import com.ojosama.eventservice.event.application.dto.result.EventResult;
import com.ojosama.eventservice.event.application.dto.result.ScheduleResult;
import com.ojosama.eventservice.event.application.service.EventCommandService;
import com.ojosama.eventservice.event.application.service.EventQueryService;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.presentation.dto.request.CreateEventRequest;
import com.ojosama.eventservice.event.presentation.dto.request.CreateScheduleRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("EventController 컨트롤러 테스트")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private EventCommandService eventCommandService;

    @MockitoBean
    private EventQueryService eventQueryService;

    private ObjectMapper objectMapper;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final String MANAGER_ROLE = "FESTIVAL_MANAGER";

    private static final LocalDateTime FUTURE_START = LocalDateTime.now().plusDays(30);
    private static final LocalDateTime FUTURE_END = LocalDateTime.now().plusDays(60);

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private CreateEventRequest buildValidRequest(boolean hasTicketing) {
        List<CreateScheduleRequest> schedules = List.of(
                new CreateScheduleRequest("메인 공연", FUTURE_START, FUTURE_START.plusHours(2))
        );

        if (hasTicketing) {
            return new CreateEventRequest(
                    "서울 재즈 페스티벌", CATEGORY_ID,
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

        return new CreateEventRequest(
                "서울 재즈 페스티벌", CATEGORY_ID,
                FUTURE_START, FUTURE_END,
                "올림픽공원", new BigDecimal("37.52"), new BigDecimal("127.12"),
                0, 50000,
                false, null, null, null,
                "http://official.example.com",
                "최고의 재즈 페스티벌", "재즈밴드A", "http://img.example.com/banner.jpg",
                schedules
        );
    }

    private EventResult buildEventResult(boolean hasTicketing) {
        List<ScheduleResult> schedules = List.of(
                new ScheduleResult(UUID.randomUUID(), "메인 공연", FUTURE_START, FUTURE_START.plusHours(2))
        );

        return new EventResult(
                UUID.randomUUID(), "서울 재즈 페스티벌",
                CATEGORY_ID, "FESTIVAL",
                FUTURE_START, FUTURE_END,
                "올림픽공원", new BigDecimal("37.52"), new BigDecimal("127.12"),
                0, 50000,
                hasTicketing,
                hasTicketing ? FUTURE_START.minusDays(10) : null,
                hasTicketing ? FUTURE_START.minusDays(1) : null,
                hasTicketing ? "http://ticket.example.com" : null,
                "http://official.example.com",
                "최고의 재즈 페스티벌", "재즈밴드A", "http://img.example.com/banner.jpg",
                "SCHEDULED",
                schedules
        );
    }

    @Nested
    @DisplayName("행사 등록 실패 (Red)")
    class CreateEventFailure {

        @Test
        @DisplayName("X-User-Id 헤더 없음 → 401")
        void createEvent_missingUserId_returns401() throws Exception {
            mockMvc.perform(post("/v1/events")
                            .header("X-User-Role", MANAGER_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildValidRequest(false))))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("X-User-Role 헤더 없음 → 401")
        void createEvent_missingUserRole_returns401() throws Exception {
            mockMvc.perform(post("/v1/events")
                            .header("X-User-Id", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildValidRequest(false))))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("name 누락 → 400")
        void createEvent_missingName_returns400() throws Exception {
            CreateEventRequest request = new CreateEventRequest(
                    null, CATEGORY_ID,
                    FUTURE_START, FUTURE_END,
                    "올림픽공원", new BigDecimal("37.52"), new BigDecimal("127.12"),
                    0, 50000,
                    false, null, null, null, null,
                    "설명", null, "http://img.example.com/banner.jpg",
                    List.of(new CreateScheduleRequest("공연", FUTURE_START, FUTURE_START.plusHours(2)))
            );

            mockMvc.perform(post("/v1/events")
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", MANAGER_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("schedules 빈 배열 → 400")
        void createEvent_emptySchedules_returns400() throws Exception {
            CreateEventRequest request = new CreateEventRequest(
                    "서울 재즈 페스티벌", CATEGORY_ID,
                    FUTURE_START, FUTURE_END,
                    "올림픽공원", new BigDecimal("37.52"), new BigDecimal("127.12"),
                    0, 50000,
                    false, null, null, null, null,
                    "설명", null, "http://img.example.com/banner.jpg",
                    List.of()
            );

            mockMvc.perform(post("/v1/events")
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", MANAGER_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("has_ticketing=true + ticketing_link 누락 → 400")
        void createEvent_ticketingTrueWithoutLink_returns400() throws Exception {
            given(eventCommandService.createEvent(any()))
                    .willThrow(new EventException(EventErrorCode.TICKETING_NOT_AVAILABLE));

            mockMvc.perform(post("/v1/events")
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", MANAGER_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildValidRequest(false))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("start_at > end_at → 400")
        void createEvent_startAfterEnd_returns400() throws Exception {
            given(eventCommandService.createEvent(any()))
                    .willThrow(new EventException(EventErrorCode.EVENT_INVALID_TIME));

            mockMvc.perform(post("/v1/events")
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", MANAGER_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildValidRequest(false))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("존재하지 않는 category_id → 404")
        void createEvent_unknownCategory_returns404() throws Exception {
            given(eventCommandService.createEvent(any()))
                    .willThrow(new EventException(EventErrorCode.EVENT_CATEGORY_NOT_FOUND));

            mockMvc.perform(post("/v1/events")
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", MANAGER_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildValidRequest(false))))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("행사 상세 조회 실패 (Red)")
    class GetEventFailure {

        @Test
        @DisplayName("X-User-Id 헤더 없음 → 401")
        void getEvent_missingUserId_returns401() throws Exception {
            mockMvc.perform(get("/v1/events/{eventId}", UUID.randomUUID())
                            .header("X-User-Role", MANAGER_ROLE))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("X-User-Role 헤더 없음 → 401")
        void getEvent_missingUserRole_returns401() throws Exception {
            mockMvc.perform(get("/v1/events/{eventId}", UUID.randomUUID())
                            .header("X-User-Id", USER_ID.toString()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("존재하지 않는 eventId → 404")
        void getEvent_unknownEventId_returns404() throws Exception {
            given(eventQueryService.getEventById(any()))
                    .willThrow(new EventException(EventErrorCode.EVENT_NOT_FOUND));

            mockMvc.perform(get("/v1/events/{eventId}", UUID.randomUUID())
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", MANAGER_ROLE))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("행사 상세 조회 성공 (Green)")
    class GetEventSuccess {

        @Test
        @DisplayName("유효한 eventId → 200, schedules 배열 포함")
        void getEvent_validEventId_returns200() throws Exception {
            UUID eventId = UUID.randomUUID();
            given(eventQueryService.getEventById(eventId)).willReturn(buildEventResult(false));

            mockMvc.perform(get("/v1/events/{eventId}", eventId)
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", MANAGER_ROLE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.status").value("SCHEDULED"))
                    .andExpect(jsonPath("$.data.schedules").isArray())
                    .andExpect(jsonPath("$.data.schedules.length()").value(1));
        }
    }

    @Nested
    @DisplayName("행사 등록 성공 (Green)")
    class CreateEventSuccess {

        @Test
        @DisplayName("MANAGER 권한 + has_ticketing=false → 201, status=SCHEDULED")
        void createEvent_managerWithoutTicketing_returns201() throws Exception {
            given(eventCommandService.createEvent(any())).willReturn(buildEventResult(false));

            mockMvc.perform(post("/v1/events")
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", MANAGER_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildValidRequest(false))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.status").value("SCHEDULED"))
                    .andExpect(jsonPath("$.data.hasTicketing").value(false))
                    .andExpect(jsonPath("$.data.schedules").isArray())
                    .andExpect(jsonPath("$.data.schedules.length()").value(1));
        }

        @Test
        @DisplayName("MANAGER 권한 + has_ticketing=true + ticketing 필드 모두 포함 → 201")
        void createEvent_managerWithTicketing_returns201() throws Exception {
            given(eventCommandService.createEvent(any())).willReturn(buildEventResult(true));

            mockMvc.perform(post("/v1/events")
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", MANAGER_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildValidRequest(true))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.status").value("SCHEDULED"))
                    .andExpect(jsonPath("$.data.hasTicketing").value(true))
                    .andExpect(jsonPath("$.data.ticketingLink").value("http://ticket.example.com"));
        }
    }
}
