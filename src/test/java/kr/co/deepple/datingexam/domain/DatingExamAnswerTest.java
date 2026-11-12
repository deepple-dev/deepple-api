package kr.co.deepple.datingexam.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DatingExamAnswerTest {

  @Test
  @DisplayName("content이 null 또는 빈 문자열인 경우 예외가 발생한다.")
  void createWithNullOrEmptyContent() {
    // Given
    Long questionId = 1L;
    String nullContent = null;
    String emptyContent = "";
    AnswerPersonalityType personalityType = AnswerPersonalityType.DECISIVE_INDEPENDENT;

    // When & Then
    assertThatThrownBy(() -> DatingExamAnswer.of(questionId, nullContent, personalityType))
        .isInstanceOf(NullPointerException.class);

    assertThatThrownBy(() -> DatingExamAnswer.of(questionId, emptyContent, personalityType))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("personalityType이 null인 경우 예외가 발생한다.")
  void createWithNullPersonalityType() {
    // Given
    Long questionId = 1L;
    String content = "Valid Answer Content";
    AnswerPersonalityType nullPersonalityType = null;

    // When & Then
    assertThatThrownBy(() -> DatingExamAnswer.of(questionId, content, nullPersonalityType))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("정상적인 answer 생성")
  void createWithValidParameters() {
    // Given
    Long questionId = 1L;
    String content = "Valid Answer Content";
    AnswerPersonalityType personalityType = AnswerPersonalityType.DEVOTED_ROMANTIC;

    // When
    DatingExamAnswer answer = DatingExamAnswer.of(questionId, content, personalityType);

    // Then
    assertThat(answer.getQuestionId()).isEqualTo(questionId);
    assertThat(answer.getContent()).isEqualTo(content);
    assertThat(answer.getPersonalityType()).isEqualTo(personalityType);
  }
}
