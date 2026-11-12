package kr.co.deepple.interview.command.domain.answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.interview.command.domain.InterviewErrorCode;
import kr.co.deepple.interview.fixture.InterviewAnswerFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InterviewAnswerTest {

  @Nested
  @DisplayName("of 메서드 테스트")
  class OfMethodTest {

    @ParameterizedTest
    @ValueSource(strings = {"body is null", "questionId is null", "memberId is null"})
    @DisplayName("of 메서드에서 필드 값이 null이면 예외를 던집니다.")
    void throwsExceptionWhenFieldValueIsNull(String fieldName) {
      // given
      Long questionId = fieldName.equals("questionId is null") ? null : 1L;
      Long memberId = fieldName.equals("memberId is null") ? null : 2L;
      String content = fieldName.equals("body is null") ? null : "body";

      // when & then
      assertThatThrownBy(() -> InterviewAnswer.of(questionId, memberId, content))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("of 메서드에서 content가 blank이면 예외를 던집니다.")
    void throwsExceptionWhenContentIsBlank() {
      // given
      Long questionId = 1L;
      Long memberId = 2L;
      String content = " ";

      // when & then
      assertThatThrownBy(() -> InterviewAnswer.of(questionId, memberId, content))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", InterviewErrorCode.INVALID_INTERVIEW_ANSWER_CONTENT);
    }

    @Test
    @DisplayName("of 메서드에서 필드 값이 정상이면 InterviewAnswer 객체를 생성합니다.")
    void createInterviewAnswerObjectWhenFieldValuesAreValid() {
      // given
      Long questionId = 1L;
      Long memberId = 2L;
      String content = "body";

      // when
      InterviewAnswer interviewAnswer = InterviewAnswer.of(questionId, memberId, content);

      // then
      assertThat(interviewAnswer).isNotNull();
    }
  }

  @Nested
  @DisplayName("updateContent 메서드 테스트")
  class UpdateContentMethodTest {

    @Test
    @DisplayName("updateContent 메서드에서 content가 null이면 예외를 던집니다.")
    void throwsExceptionWhenContentIsNull() {
      // given
      InterviewAnswer interviewAnswer = InterviewAnswerFixture.builder().build();

      // when & then
      assertThatThrownBy(() -> interviewAnswer.updateContent(null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("updateContent 메서드에서 content가 blank이면 예외를 던집니다.")
    void throwsExceptionWhenContentIsBlank() {
      // given
      InterviewAnswer interviewAnswer = InterviewAnswerFixture.builder().build();

      // when & then
      assertThatThrownBy(() -> interviewAnswer.updateContent(" "))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", InterviewErrorCode.INVALID_INTERVIEW_ANSWER_CONTENT);
    }

    @Test
    @DisplayName("updateContent 메서드에서 content가 정상이면 content를 업데이트합니다.")
    void updateContentWhenContentIsValid() {
      // given
      InterviewAnswer interviewAnswer = InterviewAnswerFixture.builder().content("content").build();

      // when
      String updatedContent = "updated content";
      interviewAnswer.updateContent(updatedContent);

      // then
      assertThat(interviewAnswer.getContent()).isEqualTo(updatedContent);
    }
  }

  @Nested
  @DisplayName("isAnsweredBy 메서드 테스트")
  class IsAnsweredByMethodTest {

    @Test
    @DisplayName("isAnsweredBy 메서드에서 memberId가 null이면 예외를 던집니다.")
    void throwsExceptionWhenMemberIdIsNull() {
      // given
      InterviewAnswer interviewAnswer = InterviewAnswerFixture.builder().build();

      // when & then
      Long nullMemberId = null;
      assertThatThrownBy(() -> interviewAnswer.isAnsweredBy(nullMemberId))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("isAnsweredBy 메서드에서 memberId가 interviewAnswer.memberId와 일치하면 true를 반환합니다.")
    void returnTrueWhenMemberIdIsValid() {
      // given
      Long memberId = 2L;
      InterviewAnswer interviewAnswer = InterviewAnswerFixture.builder().memberId(memberId).build();

      // when
      boolean result = interviewAnswer.isAnsweredBy(memberId);

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isAnsweredBy 메서드에서 memberId가 interviewAnswer.memberId와 일치하지 않으면 false를 반환합니다.")
    void returnFalseWhenMemberIdIsInvalid() {
      // given
      Long memberId = 2L;
      InterviewAnswer interviewAnswer = InterviewAnswerFixture.builder().memberId(memberId).build();

      // when
      boolean result = interviewAnswer.isAnsweredBy(3L);

      // then
      assertThat(result).isFalse();
    }
  }
}
