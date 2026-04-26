package com.ojosama.eventservice.event.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.common.exception.GlobalExceptionHandler;
import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import com.ojosama.eventservice.event.application.service.EventCategoryCommandService;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.presentation.dto.request.CreateEventCategoryRequest;
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
    private EventCategoryCommandService eventCategoryService;

    private ObjectMapper objectMapper;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String MANAGER_ROLE = "FESTIVAL_MANAGER";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("카테고리 등록 실패")
    class CreateCategoryFailure {
        @Test
        @DisplayName("X-User-Id 헤더 없음 → 401 (CustomException)")
        void createCategory_missingUserId_returns401() throws Exception {
            mockMvc.perform(post("/v1/event-categories")
                            .header("X-User-Role", ADMIN_ROLE) // ID가 없음
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateEventCategoryRequest("FESTIVAL"))))
                    .andExpect(status().isUnauthorized()); // CommonErrorCode.INVALID_TOKEN이 401인지 확인 필요
        }

        @Test
        @DisplayName("name 누락 → 400 (Validation)")
        void createCategory_missingName_returns400() throws Exception {
            mockMvc.perform(post("/v1/event-categories")
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Role", ADMIN_ROLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateEventCategoryRequest(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("중복 name → 409 (EventException)")
        void createCategory_duplicateName_returns409() throws Exception {
            given(eventCategoryService.createCategory(any()))
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
            given(eventCategoryService.createCategory(any()))
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
}
