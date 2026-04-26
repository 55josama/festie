package com.ojosama.eventservice.event.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.model.EventCategory;
import com.ojosama.eventservice.event.domain.repository.EventCategoryRepository;
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
@DisplayName("EventCategoryQueryService 단위 테스트")
class EventCategoryQueryServiceTest {

    @Mock
    private EventCategoryRepository eventCategoryRepository;

    @InjectMocks
    private EventCategoryQueryServiceImpl eventCategoryQueryService;

    private static final UUID CATEGORY_ID = UUID.randomUUID();

    @Nested
    @DisplayName("카테고리 목록 조회")
    class GetCategories {

        @Test
        @DisplayName("성공: 전체 카테고리 목록을 반환한다")
        void getCategories_success() {
            given(eventCategoryRepository.findAll()).willReturn(List.of(
                    EventCategory.create("FESTIVAL"),
                    EventCategory.create("CONCERT")));

            List<EventCategoryResult> result = eventCategoryQueryService.getCategories();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(EventCategoryResult::name)
                    .containsExactly("FESTIVAL", "CONCERT");
        }

        @Test
        @DisplayName("성공: 카테고리가 없으면 빈 목록을 반환한다")
        void getCategories_empty() {
            given(eventCategoryRepository.findAll()).willReturn(List.of());

            List<EventCategoryResult> result = eventCategoryQueryService.getCategories();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("카테고리 단건 조회")
    class GetCategoryById {

        @Test
        @DisplayName("성공: 존재하는 categoryId로 조회한다")
        void getCategoryById_success() {
            given(eventCategoryRepository.findById(CATEGORY_ID))
                    .willReturn(Optional.of(EventCategory.create("FESTIVAL")));

            EventCategoryResult result = eventCategoryQueryService.getCategoryById(CATEGORY_ID);

            assertThat(result.name()).isEqualTo("FESTIVAL");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 categoryId → NOT_FOUND")
        void getCategoryById_notFound_throwsException() {
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> eventCategoryQueryService.getCategoryById(CATEGORY_ID))
                    .isInstanceOf(EventException.class)
                    .hasMessageContaining(EventErrorCode.EVENT_CATEGORY_NOT_FOUND.getMessage());
        }
    }
}
