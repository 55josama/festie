package com.ojosama.eventservice.event.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.common.exception.GlobalExceptionHandler;
import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import com.ojosama.eventservice.event.application.service.EventCategoryCommandService;
import com.ojosama.eventservice.event.application.service.EventCategoryQueryService;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.presentation.dto.request.CreateEventCategoryRequest;
import com.ojosama.eventservice.event.presentation.dto.request.UpdateEventCategoryRequest;
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

@WebMvcTest(EventCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("EventCategoryController 컨트롤러 테스트")
class EventCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private EventCategoryCommandService eventCategoryCommandService;


    private ObjectMapper objectMapper;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final String ADMIN_ROLE = "ADMIN";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // ──────────────────────────────────────────────
    // POST /v1/event-categories
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("카테고리 등록 실패")
    class CreateCategoryFailure {

        @Test
        @DisplayName("X-User-Id 헤더 없음 → 401")
        void createCategory_missingUserId_returns401() throws Exception {
            mockMvc.perform(post("/v1/event-categories")
                            .header("X-User-Role", ADMIN_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateEventCategoryRequest("FESTIVAL"))))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("name 누락 → 400")
        void createCategory_missingName_returns400() throws Exception {
            mockMvc.perform(post("/v1/event-categories")
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", ADMIN_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateEventCategoryRequest(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("중복 name → 409")
        void createCategory_duplicateName_returns409() throws Exception {
            given(eventCategoryCommandService.createCategory(any()))
                    .willThrow(new EventException(EventErrorCode.EVENT_CATEGORY_ALREADY_EXISTS));

            mockMvc.perform(post("/v1/event-categories")
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", ADMIN_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateEventCategoryRequest("FESTIVAL"))))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("카테고리 등록 성공")
    class CreateCategorySuccess {

        @Test
        @DisplayName("성공 → 201 반환")
        void createCategory_success() throws Exception {
            UUID categoryId = UUID.randomUUID();
            given(eventCategoryCommandService.createCategory(any()))
                    .willReturn(new EventCategoryResult(categoryId, "FESTIVAL"));

            mockMvc.perform(post("/v1/event-categories")
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", ADMIN_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateEventCategoryRequest("FESTIVAL"))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.name").value("FESTIVAL"));
        }
    }
    // ──────────────────────────────────────────────
    // PATCH /v1/event-categories/{categoryId}
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("카테고리 수정 실패")
    class UpdateCategoryFailure {

        @Test
        @DisplayName("X-User-Id 헤더 없음 → 401")
        void updateCategory_missingUserId_returns401() throws Exception {
            mockMvc.perform(patch("/v1/event-categories/{id}", CATEGORY_ID)
                            .header("X-User-Role", ADMIN_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UpdateEventCategoryRequest("CONCERT"))))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("name 누락 → 400")
        void updateCategory_missingName_returns400() throws Exception {
            mockMvc.perform(patch("/v1/event-categories/{id}", CATEGORY_ID)
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", ADMIN_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UpdateEventCategoryRequest(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("존재하지 않는 categoryId → 404")
        void updateCategory_notFound_returns404() throws Exception {
            given(eventCategoryCommandService.updateCategory(any()))
                    .willThrow(new EventException(EventErrorCode.EVENT_CATEGORY_NOT_FOUND));

            mockMvc.perform(patch("/v1/event-categories/{id}", CATEGORY_ID)
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", ADMIN_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UpdateEventCategoryRequest("CONCERT"))))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("카테고리 수정 성공")
    class UpdateCategorySuccess {

        @Test
        @DisplayName("ADMIN + 유효 요청 → 200, 수정된 name 반환")
        void updateCategory_success() throws Exception {
            given(eventCategoryCommandService.updateCategory(any()))
                    .willReturn(new EventCategoryResult(CATEGORY_ID, "CONCERT"));

            mockMvc.perform(patch("/v1/event-categories/{id}", CATEGORY_ID)
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", ADMIN_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UpdateEventCategoryRequest("CONCERT"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(CATEGORY_ID.toString()))
                    .andExpect(jsonPath("$.data.name").value("CONCERT"));
        }
    }

}
