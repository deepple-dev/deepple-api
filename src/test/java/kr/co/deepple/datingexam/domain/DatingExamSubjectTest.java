package kr.co.deepple.datingexam.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DatingExamSubjectTest {

  @Test
  @DisplayName("name이 빈 문자열인 경우 예외가 발생한다.")
  void createWithEmptyName() {
    // Given
    String emptyName = "";
    SubjectType subjectType = SubjectType.REQUIRED;

    // When & Then
    assertThatThrownBy(() -> DatingExamSubject.of(emptyName, subjectType))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("정상적인 subject 생성")
  void createWithValidParameters() {
    // Given
    String subjectName = "Valid Subject";
    SubjectType subjectType = SubjectType.REQUIRED;

    // When
    DatingExamSubject subject = DatingExamSubject.of(subjectName, subjectType);

    // Then
    assertThat(subject.getName()).isEqualTo(subjectName);
    assertThat(subject.getType()).isEqualTo(subjectType);
    assertThat(subject.isPublic()).isTrue();
  }
}
