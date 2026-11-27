package kr.co.deepple.interview.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.interview.fixture.InterviewQuestionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InterviewQuestionTest {

  @Nested
  @DisplayName("of 메서드 테스트")
  class ofMethodTest {

    @Test
    @DisplayName("of 메서드에서 content가 blank이면 예외를 던집니다.")
    void throwsExceptionWhenContentIsBlank() {
      // given
      String content = " ";
      InterviewCategory interviewCategory = InterviewCategory.PERSONAL;
      boolean isPublic = true;

      // when & then
      assertThatThrownBy(() -> InterviewQuestion.of(content, interviewCategory, isPublic, null))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", InterviewErrorCode.INVALID_INTERVIEW_QUESTION_CONTENT);
    }

    @Test
    @DisplayName("of 메서드에서 필드 값이 정상이면 InterviewQuestion 객체를 생성합니다.")
    void createInterviewQuestionObjectWhenFieldValuesAreValid() {
      // given
      String content = "body";
      InterviewCategory interviewCategory = InterviewCategory.PERSONAL;
      boolean isPublic = true;

      // when
      InterviewQuestion interviewQuestion =
          InterviewQuestion.of(content, interviewCategory, isPublic, null);

      // then
      assertThat(interviewQuestion).isNotNull();
    }
  }

  @Nested
  @DisplayName("update 메서드 테스트")
  class updateMethodTest {

    @Test
    @DisplayName("update 메서드에서 content가 blank이면 예외를 던집니다.")
    void throwsExceptionWhenContentIsBlank() {
      // given
      InterviewQuestion interviewQuestion = InterviewQuestionFixture.builder().build();
      String content = " ";
      InterviewCategory interviewCategory = InterviewCategory.SOCIAL;
      boolean isPublic = false;

      // when & then
      assertThatThrownBy(() -> interviewQuestion.update(content, interviewCategory, isPublic, null))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", InterviewErrorCode.INVALID_INTERVIEW_QUESTION_CONTENT);
    }

    @Test
    @DisplayName("update 메서드에서 필드 값이 정상이면 InterviewQuestion 객체를 업데이트 합니다.")
    void updateInterviewQuestionObjectWhenFieldValuesAreValid() {
      // given
      InterviewQuestion interviewQuestion = InterviewQuestionFixture.builder().build();
      String content = "updated body";
      InterviewCategory interviewCategory = InterviewCategory.SOCIAL;
      boolean isPublic = false;

      // when
      interviewQuestion.update(content, interviewCategory, isPublic, "설명");

      // then
      assertThat(interviewQuestion).isNotNull();
      assertThat(interviewQuestion.getContent()).isEqualTo(content);
      assertThat(interviewQuestion.isPublic()).isEqualTo(isPublic);
      assertThat(interviewQuestion.getDescription()).isEqualTo("설명");
    }
  }

  @Test
  @DisplayName("수정할 때 설명을 보내지 않으면 기존 설명을 그대로 두고, 빈 문자열이면 지운다.")
  void keepDescriptionWhenNullAndClearWhenBlank() {
    InterviewQuestion question = InterviewQuestion.of("질문", InterviewCategory.PERSONAL, true, "설명");

    question.update("질문", InterviewCategory.PERSONAL, false, null);
    assertThat(question.getDescription()).isEqualTo("설명");

    question.update("질문", InterviewCategory.PERSONAL, false, " ");
    assertThat(question.getDescription()).isNull();
  }
}
