package com.ojosama.eventservice.event.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ojosama.common.exception.GlobalExceptionHandler;
import com.ojosama.eventservice.event.application.dto.result.EventResult;
import com.ojosama.eventservice.event.application.dto.result.ScheduleResult;
import com.ojosama.eventservice.event.application.service.EventQueryService;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("EventQueryController 테스트")
class EventQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private EventQueryService eventQueryService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final String MANAGER_ROLE = "FESTIVAL_MANAGER";

    private static final LocalDateTime FUTURE_START = LocalDateTime.now().plusDays(30);
    private static final LocalDateTime FUTURE_END = LocalDateTime.now().plusDays(60);

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
    @DisplayName("행사 목록 조회 실패")
    class GetEventsFailure {

        @Test
        @DisplayName("X-User-Id 미전달 → 401")
        void getEvents_missingUserId_returns401() throws Exception {
            mockMvc.perform(get("/v1/events")
                            .header("X-User-Role", MANAGER_ROLE))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("X-User-Role 미전달 → 401")
        void getEvents_missingUserRole_returns401() throws Exception {
            mockMvc.perform(get("/v1/events")
                            .header("X-User-Id", USER_ID.toString()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("행사 목록 조회 성공")
    class GetEventsSuccess {

        @Test
        @DisplayName("전체 조회 → 200, 페이지 정보 포함")
        void getEvents_noFilter_returns200WithPage() throws Exception {
            given(eventQueryService.getEvents(any(), any()))
                    .willReturn(new PageImpl<>(List.of(buildEventResult(false)), PageRequest.of(0, 10), 1));

            mockMvc.perform(get("/v1/events")
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", MANAGER_ROLE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.totalPages").value(1));
        }

        @Test
        @DisplayName("조회 결과 없을 때 빈 목록 반환 → 200")
        void getEvents_noResult_returnsEmptyContent() throws Exception {
            given(eventQueryService.getEvents(any(), any()))
                    .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

            mockMvc.perform(get("/v1/events")
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", MANAGER_ROLE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(0));
        }
    }

    @Nested
    @DisplayName("행사 상세 조회 실패")
    class GetEventFailure {

        @Test
        @DisplayName("X-User-Id 미전달 → 401")
        void getEvent_missingUserId_returns401() throws Exception {
            mockMvc.perform(get("/v1/events/{eventId}", UUID.randomUUID())
                            .header("X-User-Role", MANAGER_ROLE))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("X-User-Role 미전달 → 401")
        void getEvent_missingUserRole_returns401() throws Exception {
            mockMvc.perform(get("/v1/events/{eventId}", UUID.randomUUID())
                            .header("X-User-Id", USER_ID.toString()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("존재하지 않는 행사 ID → 404")
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
    @DisplayName("행사 상세 조회 성공")
    class GetEventSuccess {

        @Test
        @DisplayName("정상 조회 → 200, schedules 포함")
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
}
