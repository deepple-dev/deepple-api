package kr.co.deepple.datingexam.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import kr.co.deepple.datingexam.domain.dto.DatingExamSubmitRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DatingExamSubmitTest {

  @Nested
  @DisplayName("create 메서드 테스트")
  class CreateMethodTest {

    @Test
    @DisplayName("memberId가 null이면 예외가 발생한다")
    void createWithNullMemberId() {
      // Given
      Long nullMemberId = null;
      Long subjectId = 2L;
      String encodedAnswers = "encodedAnswers";

      DatingExamSubmitRequest request = mock(DatingExamSubmitRequest.class);
      when(request.subjectId()).thenReturn(subjectId);
      DatingExamAnswerEncoder encoder = mock(DatingExamAnswerEncoder.class);
      when(encoder.encode(request)).thenReturn(encodedAnswers);
      // When & Then

      assertThatThrownBy(() -> DatingExamSubmit.from(request, encoder, nullMemberId))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("subjectId가 null이면 예외가 발생한다")
    void createWithNullSubjectId() {
      // Given
      Long memberId = 1L;
      Long nullSubjectId = null;
      String encodedAnswers = "encodedAnswers";

      DatingExamSubmitRequest request = mock(DatingExamSubmitRequest.class);
      when(request.subjectId()).thenReturn(nullSubjectId);
      DatingExamAnswerEncoder encoder = mock(DatingExamAnswerEncoder.class);
      when(encoder.encode(request)).thenReturn(encodedAnswers);

      // When & Then
      assertThatThrownBy(() -> DatingExamSubmit.from(request, encoder, memberId))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("정상적인 memberId로 DatingExamSubmit 객체를 생성한다")
    void createWithValidMemberId() {
      // Given
      Long memberId = 1L;
      Long subjectId = 2L;
      String encodedAnswers = "encodedAnswers";

      DatingExamSubmitRequest request = mock(DatingExamSubmitRequest.class);
      when(request.subjectId()).thenReturn(subjectId);
      DatingExamAnswerEncoder encoder = mock(DatingExamAnswerEncoder.class);
      when(encoder.encode(request)).thenReturn(encodedAnswers);

      // When
      DatingExamSubmit datingExamSubmit = DatingExamSubmit.from(request, encoder, memberId);

      // Then
      assertThat(datingExamSubmit.getMemberId()).isEqualTo(memberId);
      assertThat(datingExamSubmit.getSubjectId()).isEqualTo(subjectId);
      assertThat(datingExamSubmit.getAnswers()).isEqualTo(encodedAnswers);
    }
  }
}
