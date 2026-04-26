package com.ojosama.eventservice.event.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ojosama.eventservice.event.application.dto.command.CreateEventCategoryCommand;
import com.ojosama.eventservice.event.application.dto.command.UpdateEventCategoryCommand;
import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.model.EventCategory;
import com.ojosama.eventservice.event.domain.repository.EventCategoryRepository;
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
@DisplayName("EventCategoryCommandService 단위 테스트")
class EventCategoryCommandServiceTest {

    @Mock
    private EventCategoryRepository eventCategoryRepository;

    @InjectMocks
    private EventCategoryCommandServiceImpl eventCategoryService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final String CATEGORY_NAME = "FESTIVAL";

    @Nested
    @DisplayName("카테고리 등록")
    class CreateCategory {

        @Test
        @DisplayName("성공: 중복되지 않은 이름이면 카테고리를 저장한다")
        void createCategory_success() {
            CreateEventCategoryCommand command = new CreateEventCategoryCommand(USER_ID, CATEGORY_NAME);
            given(eventCategoryRepository.existsByName(CATEGORY_NAME)).willReturn(false);
            given(eventCategoryRepository.save(any(EventCategory.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            EventCategoryResult result = eventCategoryService.createCategory(command);

            assertThat(result.name()).isEqualTo(CATEGORY_NAME);
            verify(eventCategoryRepository).existsByName(CATEGORY_NAME);
            verify(eventCategoryRepository).save(any(EventCategory.class));
        }

        @Test
        @DisplayName("실패: 이미 존재하는 이름이면 예외가 발생한다")
        void createCategory_duplicateName_throwsException() {
            CreateEventCategoryCommand command = new CreateEventCategoryCommand(USER_ID, CATEGORY_NAME);
            given(eventCategoryRepository.existsByName(CATEGORY_NAME)).willReturn(true);

            assertThatThrownBy(() -> eventCategoryService.createCategory(command))
                    .isInstanceOf(EventException.class)
                    .hasMessageContaining(EventErrorCode.EVENT_CATEGORY_ALREADY_EXISTS.getMessage());

            verify(eventCategoryRepository, never()).save(any(EventCategory.class));
        }
    }

    @Nested
    @DisplayName("카테고리 수정")
    class UpdateCategory {

        @Test
        @DisplayName("성공: 존재하는 카테고리의 이름을 수정한다")
        void updateCategory_success() {
            EventCategory category = EventCategory.create(CATEGORY_NAME);
            UpdateEventCategoryCommand command = new UpdateEventCategoryCommand(USER_ID, CATEGORY_ID, "CONCERT");
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(category));
            given(eventCategoryRepository.existsByName("CONCERT")).willReturn(false);

            EventCategoryResult result = eventCategoryService.updateCategory(command);

            assertThat(result.name()).isEqualTo("CONCERT");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 categoryId → NOT_FOUND")
        void updateCategory_notFound_throwsException() {
            UpdateEventCategoryCommand command = new UpdateEventCategoryCommand(USER_ID, CATEGORY_ID, "CONCERT");
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> eventCategoryService.updateCategory(command))
                    .isInstanceOf(EventException.class)
                    .hasMessageContaining(EventErrorCode.EVENT_CATEGORY_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("실패: 중복된 이름으로 수정 시 예외가 발생한다")
        void updateCategory_duplicateName_throwsException() {
            EventCategory category = EventCategory.create(CATEGORY_NAME);
            UpdateEventCategoryCommand command = new UpdateEventCategoryCommand(USER_ID, CATEGORY_ID, "CONCERT");
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(category));
            given(eventCategoryRepository.existsByName("CONCERT")).willReturn(true);

            assertThatThrownBy(() -> eventCategoryService.updateCategory(command))
                    .isInstanceOf(EventException.class)
                    .hasMessageContaining(EventErrorCode.EVENT_CATEGORY_ALREADY_EXISTS.getMessage());
        }
    }

    @Nested
    @DisplayName("카테고리 삭제")
    class DeleteCategory {

        @Test
        @DisplayName("성공: 존재하는 카테고리를 소프트 삭제한다")
        void deleteCategory_success() {
            EventCategory category = EventCategory.create(CATEGORY_NAME);
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(category));

            eventCategoryService.deleteCategory(USER_ID, CATEGORY_ID);

            assertThat(category.getDeletedAt()).isNotNull();
            assertThat(category.getDeletedBy()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 categoryId → NOT_FOUND")
        void deleteCategory_notFound_throwsException() {
            given(eventCategoryRepository.findById(CATEGORY_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> eventCategoryService.deleteCategory(USER_ID, CATEGORY_ID))
                    .isInstanceOf(EventException.class)
                    .hasMessageContaining(EventErrorCode.EVENT_CATEGORY_NOT_FOUND.getMessage());
        }
    }
}
