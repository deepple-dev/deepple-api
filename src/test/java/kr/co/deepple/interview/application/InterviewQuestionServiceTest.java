package kr.co.deepple.interview.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.interview.application.dto.InterviewQuestionSaveRequest;
import kr.co.deepple.interview.application.port.out.InterviewQuestionRepository;
import kr.co.deepple.interview.domain.InterviewCategory;
import kr.co.deepple.interview.domain.InterviewErrorCode;
import kr.co.deepple.interview.domain.InterviewQuestion;
import kr.co.deepple.interview.fixture.InterviewQuestionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterviewQuestionServiceTest {

  @Mock private InterviewQuestionRepository interviewQuestionRepository;

  @InjectMocks private InterviewQuestionService interviewQuestionService;

  @Nested
  @DisplayName("createQuestion 메서드 테스트")
  class createQuestionTest {

    @Test
    @DisplayName("인터뷰 질문 내용이 이미 존재하면 예외를 던진다.")
    void throwsExceptionWhenInterviewQuestionContentAlreadyExists() {
      // given
      String content = "content";
      String category = InterviewCategory.PERSONAL.name();
      boolean isPublic = true;
      InterviewQuestionSaveRequest request =
          new InterviewQuestionSaveRequest(content, category, isPublic);

      when(interviewQuestionRepository.existsByContent(content)).thenReturn(true);

      // when & then
      assertThatThrownBy(() -> interviewQuestionService.createQuestion(request))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", InterviewErrorCode.INTERVIEW_QUESTION_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("인터뷰 카테고리가 존재하지 않으면 예외를 던진다.")
    void throwsExceptionWhenInterviewCategoryDoesNotExist() {
      // given
      String content = "content";
      String category = "notExist";
      boolean isPublic = true;
      InterviewQuestionSaveRequest request =
          new InterviewQuestionSaveRequest(content, category, isPublic);

      when(interviewQuestionRepository.existsByContent(content)).thenReturn(false);

      // when & then
      assertThatThrownBy(() -> interviewQuestionService.createQuestion(request))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", InterviewErrorCode.INVALID_INTERVIEW_CATEGORY);
    }

    @Test
    @DisplayName("인터뷰 질문을 생성한다.")
    void createInterviewQuestion() {
      // given
      String content = "content";
      String category = InterviewCategory.PERSONAL.name();
      boolean isPublic = true;
      InterviewQuestionSaveRequest request =
          new InterviewQuestionSaveRequest(content, category, isPublic);

      when(interviewQuestionRepository.existsByContent(content)).thenReturn(false);

      // when
      interviewQuestionService.createQuestion(request);

      // then
      verify(interviewQuestionRepository).save(any(InterviewQuestion.class));
    }
  }

  @Nested
  @DisplayName("updateQuestion 메서드 테스트")
  class updateQuestionTest {

    @Test
    @DisplayName("인터뷰 질문이 존재하지 않으면 예외를 던진다.")
    void throwsExceptionWhenInterviewQuestionDoesNotExist() {
      // given
      Long questionId = 1L;
      String content = "content";
      String category = InterviewCategory.PERSONAL.name();
      boolean isPublic = true;
      InterviewQuestionSaveRequest request =
          new InterviewQuestionSaveRequest(content, category, isPublic);

      when(interviewQuestionRepository.findById(questionId)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> interviewQuestionService.updateQuestion(questionId, request))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND);
    }

    @Test
    @DisplayName("업데이트 하려는 인터뷰 카테고리가 존재하지 않으면 예외를 던진다.")
    void throwsExceptionWhenInterviewCategoryDoesNotExist() {
      // given
      Long questionId = 1L;
      InterviewQuestion interviewQuestion =
          InterviewQuestionFixture.builder().id(questionId).build();

      String updatedContent = "updated content";
      String updatedCategory = "notExist";
      boolean updatedIsPublic = false;
      InterviewQuestionSaveRequest request =
          new InterviewQuestionSaveRequest(updatedContent, updatedCategory, updatedIsPublic);

      when(interviewQuestionRepository.findById(questionId))
          .thenReturn(Optional.of(interviewQuestion));

      // when & then
      assertThatThrownBy(() -> interviewQuestionService.updateQuestion(questionId, request))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", InterviewErrorCode.INVALID_INTERVIEW_CATEGORY);
    }

    @Test
    @DisplayName("업데이트 하려는 인터뷰 질문 내용이 기존 질문 내용과 다르고, 해당 내용의 질문이 이미 존재하면 예외를 던진다.")
    void throwsExceptionWhenInterviewQuestionContentAlreadyExists() {
      // given
      Long questionId = 1L;
      String content = "content";
      InterviewQuestion interviewQuestion =
          InterviewQuestionFixture.builder().id(questionId).content(content).build();

      String updatedContent = "updated content";
      String updatedCategory = InterviewCategory.SOCIAL.name();
      boolean updatedIsPublic = false;
      InterviewQuestionSaveRequest request =
          new InterviewQuestionSaveRequest(updatedContent, updatedCategory, updatedIsPublic);

      when(interviewQuestionRepository.findById(questionId))
          .thenReturn(Optional.of(interviewQuestion));
      when(interviewQuestionRepository.existsByContent(updatedContent)).thenReturn(true);

      // when & then
      assertThatThrownBy(() -> interviewQuestionService.updateQuestion(questionId, request))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", InterviewErrorCode.INTERVIEW_QUESTION_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("인터뷰 질문을 업데이트한다.")
    void updateInterviewQuestion() {
      // given
      Long questionId = 1L;
      String content = "content";
      InterviewQuestion interviewQuestion =
          InterviewQuestionFixture.builder().id(questionId).content(content).build();

      String updatedContent = "updated content";
      String updatedCategory = InterviewCategory.SOCIAL.name();
      boolean updatedIsPublic = false;
      InterviewQuestionSaveRequest request =
          new InterviewQuestionSaveRequest(updatedContent, updatedCategory, updatedIsPublic);

      when(interviewQuestionRepository.findById(questionId))
          .thenReturn(Optional.of(interviewQuestion));
      when(interviewQuestionRepository.existsByContent(updatedContent)).thenReturn(false);

      // when
      interviewQuestionService.updateQuestion(questionId, request);

      // then
      assertThat(interviewQuestion.getContent()).isEqualTo(updatedContent);
      assertThat(interviewQuestion.getCategory()).isEqualTo(InterviewCategory.SOCIAL);
      assertThat(interviewQuestion.isPublic()).isFalse();
    }

    @Test
    @DisplayName("인터뷰 질문 내용이 기존 질문 내용과 동일하면 업데이트한다.")
    void updateInterviewQuestionWhenContentIsSame() {
      // given
      Long questionId = 1L;
      String content = "content";
      InterviewQuestion interviewQuestion =
          InterviewQuestionFixture.builder().id(questionId).content(content).build();

      String updatedContent = "updated content";
      String updatedCategory = InterviewCategory.SOCIAL.name();
      boolean updatedIsPublic = false;
      InterviewQuestionSaveRequest request =
          new InterviewQuestionSaveRequest(updatedContent, updatedCategory, updatedIsPublic);

      when(interviewQuestionRepository.findById(questionId))
          .thenReturn(Optional.of(interviewQuestion));

      // when
      interviewQuestionService.updateQuestion(questionId, request);

      // then
      assertThat(interviewQuestion.getContent()).isEqualTo(updatedContent);
      assertThat(interviewQuestion.getCategory()).isEqualTo(InterviewCategory.SOCIAL);
      assertThat(interviewQuestion.isPublic()).isFalse();
    }
  }
}
