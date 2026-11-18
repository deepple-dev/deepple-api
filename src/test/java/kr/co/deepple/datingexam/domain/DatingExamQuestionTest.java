package kr.co.deepple.datingexam.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DatingExamQuestionTest {

  @Test
  @DisplayName("content가 빈 문자열인 경우 예외가 발생한다.")
  void createWithEmptyContent() {
    // Given
    Long subjectId = 1L;
    String emptyContent = "";

    // When & Then
    assertThatThrownBy(() -> DatingExamQuestion.of(subjectId, emptyContent))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("정상적인 question 생성")
  void createWithValidParameters() {
    // Given
    Long subjectId = 1L;
    String content = "Valid Question Content";

    // When
    DatingExamQuestion question = DatingExamQuestion.of(subjectId, content);

    // Then
    assertThat(question.getSubjectId()).isEqualTo(subjectId);
    assertThat(question.getContent()).isEqualTo(content);
  }
}
