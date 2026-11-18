package kr.co.deepple.datingexam.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DatingExamAnswerTest {

  @Test
  @DisplayName("content가 빈 문자열인 경우 예외가 발생한다.")
  void createWithEmptyContent() {
    // Given
    Long questionId = 1L;
    String emptyContent = "";
    AnswerPersonalityType personalityType = AnswerPersonalityType.DECISIVE_INDEPENDENT;

    // When & Then
    assertThatThrownBy(() -> DatingExamAnswer.of(questionId, emptyContent, personalityType))
        .isInstanceOf(IllegalArgumentException.class);
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
