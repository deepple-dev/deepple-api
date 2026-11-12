package kr.co.deepple.datingexam.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.common.event.Events;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.application.dto.AllRequiredSubjectSubmittedEvent;
import kr.co.deepple.datingexam.application.dto.DatingExamInfoResponse;
import kr.co.deepple.datingexam.application.port.out.DatingExamAnswerRepository;
import kr.co.deepple.datingexam.application.port.out.DatingExamQueryRepository;
import kr.co.deepple.datingexam.application.port.out.DatingExamSubjectRepository;
import kr.co.deepple.datingexam.application.port.out.DatingExamSubmitRepository;
import kr.co.deepple.datingexam.application.port.out.DatingExamSubmitResultRepository;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import kr.co.deepple.datingexam.domain.DatingExamAnswer;
import kr.co.deepple.datingexam.domain.DatingExamAnswerEncoder;
import kr.co.deepple.datingexam.domain.DatingExamErrorCode;
import kr.co.deepple.datingexam.domain.DatingExamSubject;
import kr.co.deepple.datingexam.domain.DatingExamSubmit;
import kr.co.deepple.datingexam.domain.DatingExamSubmitResult;
import kr.co.deepple.datingexam.domain.SubjectType;
import kr.co.deepple.datingexam.domain.dto.AnswerSubmitRequest;
import kr.co.deepple.datingexam.domain.dto.DatingExamSubmitRequest;
import kr.co.deepple.datingexam.fixture.DatingExamAnswerFixture;
import kr.co.deepple.datingexam.fixture.DatingExamSubjectFixture;
import kr.co.deepple.datingexam.fixture.DatingExamSubmitFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatingExamModifyServiceTest {

  @InjectMocks private DatingExamModifyService datingExamModifyService;

  @Mock private DatingExamSubjectRepository datingExamSubjectRepository;

  @Mock private DatingExamSubmitRepository datingExamSubmitRepository;

  @Mock private DatingExamQueryRepository datingExamQueryRepository;

  @Mock private DatingExamAnswerEncoder answerEncoder;

  @Mock private DatingExamAnswerRepository datingExamAnswerRepository;

  @Mock private DatingExamSubmitResultRepository datingExamSubmitResultRepository;

  @Nested
  @DisplayName("submitSubject 메서드 테스트")
  class SubmitSubjectTests {

    @Test
    @DisplayName("과목 제출 기록이 없고 유효한 제출 요청이 주어지면, 모의고사 제출을 생성하고 저장한다.")
    void submitSubject_Success() {
      // Given
      Long memberId = 1L;
      Long subjectId = 2L;
      Long answerId = 10L;
      DatingExamSubmitRequest request = mock(DatingExamSubmitRequest.class);
      when(request.subjectId()).thenReturn(subjectId);
      when(request.answers()).thenReturn(List.of(new AnswerSubmitRequest(1L, answerId)));

      DatingExamSubject subject = mock(DatingExamSubject.class);
      when(subject.getType()).thenReturn(SubjectType.REQUIRED);
      when(datingExamSubjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

      when(datingExamSubmitRepository.existsByMemberIdAndSubjectId(memberId, subjectId))
          .thenReturn(false);

      DatingExamInfoResponse infoResponse = mock(DatingExamInfoResponse.class);
      when(datingExamQueryRepository.findDatingExamInfo(SubjectType.REQUIRED))
          .thenReturn(infoResponse);

      DatingExamAnswer answer =
          DatingExamAnswerFixture.builder()
              .id(answerId)
              .personalityType(AnswerPersonalityType.DECISIVE_INDEPENDENT)
              .build();
      when(datingExamAnswerRepository.findAllByIdIn(List.of(answerId))).thenReturn(List.of(answer));
      when(datingExamSubmitResultRepository.findByMemberIdForUpdate(memberId))
          .thenReturn(Optional.empty());

      try (MockedStatic<DatingExamSubmit> mockedDatingExamSubmit =
              mockStatic(DatingExamSubmit.class);
          MockedStatic<DatingExamSubmitRequestValidator> mockedValidator =
              mockStatic(DatingExamSubmitRequestValidator.class)) {
        DatingExamSubmit submit =
            DatingExamSubmitFixture.builder().memberId(memberId).subjectId(subjectId).build();
        mockedDatingExamSubmit
            .when(() -> DatingExamSubmit.from(request, answerEncoder, memberId))
            .thenReturn(submit);
        mockedValidator
            .when(() -> DatingExamSubmitRequestValidator.validateSubmit(request, infoResponse))
            .thenAnswer(invocation -> null);

        // When
        datingExamModifyService.submitSubject(request, memberId);

        // Then
        mockedDatingExamSubmit.verify(
            () -> DatingExamSubmit.from(request, answerEncoder, memberId));
        verify(datingExamSubmitRepository).save(submit);
        verify(datingExamSubmitResultRepository).save(any(DatingExamSubmitResult.class));
      }
    }

    @Test
    @DisplayName("존재하지 않는 과목 ID가 주어지면 예외를 던진다.")
    void throwIfSubjectNotFound() {
      // Given
      Long memberId = 1L;
      Long subjectId = 2L;
      DatingExamSubmitRequest request = mock(DatingExamSubmitRequest.class);
      when(request.subjectId()).thenReturn(subjectId);
      when(datingExamSubjectRepository.findById(subjectId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> datingExamModifyService.submitSubject(request, memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", DatingExamErrorCode.DATING_EXAM_SUBJECT_NOT_FOUND);
    }

    @Test
    @DisplayName("이미 과목 제출 기록이 있는 경우 예외를 던진다.")
    void throwIfAlreadySubmitted() {
      // Given
      Long memberId = 1L;
      Long subjectId = 2L;
      DatingExamSubmitRequest request = mock(DatingExamSubmitRequest.class);
      when(request.subjectId()).thenReturn(subjectId);

      DatingExamSubject subject = DatingExamSubjectFixture.builder().id(subjectId).build();
      when(datingExamSubjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

      when(datingExamSubmitRepository.existsByMemberIdAndSubjectId(memberId, subjectId))
          .thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> datingExamModifyService.submitSubject(request, memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", DatingExamErrorCode.DATING_EXAM_ALREADY_SUBMITTED);
    }

    @Test
    @DisplayName("필수 과목 제출 후 모든 필수 과목이 제출된 경우 이벤트를 발행한다.")
    void publishEventWhenAllRequiredSubjectsSubmitted() {
      // Given
      Long memberId = 1L;
      Long subjectId = 2L;
      Long answerId = 10L;
      DatingExamSubmitRequest request = mock(DatingExamSubmitRequest.class);
      when(request.subjectId()).thenReturn(subjectId);
      when(request.answers()).thenReturn(List.of(new AnswerSubmitRequest(1L, answerId)));

      DatingExamSubject subject =
          DatingExamSubjectFixture.builder().id(subjectId).type(SubjectType.REQUIRED).build();
      when(datingExamSubjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

      when(datingExamSubmitRepository.existsByMemberIdAndSubjectId(memberId, subjectId))
          .thenReturn(false);

      DatingExamInfoResponse infoResponse = mock(DatingExamInfoResponse.class);
      when(datingExamQueryRepository.findDatingExamInfo(SubjectType.REQUIRED))
          .thenReturn(infoResponse);

      DatingExamAnswer answer =
          DatingExamAnswerFixture.builder()
              .id(answerId)
              .personalityType(AnswerPersonalityType.DECISIVE_INDEPENDENT)
              .build();
      when(datingExamAnswerRepository.findAllByIdIn(List.of(answerId))).thenReturn(List.of(answer));
      when(datingExamSubmitResultRepository.findByMemberIdForUpdate(memberId))
          .thenReturn(Optional.empty());

      try (MockedStatic<DatingExamSubmit> mockedDatingExamSubmit =
              mockStatic(DatingExamSubmit.class);
          MockedStatic<DatingExamSubmitRequestValidator> mockedValidator =
              mockStatic(DatingExamSubmitRequestValidator.class);
          MockedStatic<Events> mockedEvents = mockStatic(Events.class)) {

        DatingExamSubmit submit =
            DatingExamSubmitFixture.builder().memberId(memberId).subjectId(subjectId).build();
        mockedDatingExamSubmit
            .when(() -> DatingExamSubmit.from(request, answerEncoder, memberId))
            .thenReturn(submit);
        mockedValidator
            .when(() -> DatingExamSubmitRequestValidator.validateSubmit(request, infoResponse))
            .thenAnswer(invocation -> null);
        when(datingExamSubjectRepository.findAllByType(SubjectType.REQUIRED))
            .thenReturn(Set.of(subject));

        when(datingExamSubmitRepository.findAllByMemberId(memberId)).thenReturn(Set.of(submit));

        // When
        datingExamModifyService.submitSubject(request, memberId);

        // Then
        mockedDatingExamSubmit.verify(
            () -> DatingExamSubmit.from(request, answerEncoder, memberId));
        verify(datingExamSubmitRepository).save(submit);
        verify(datingExamSubmitResultRepository).save(any(DatingExamSubmitResult.class));
        mockedEvents.verify(() -> Events.raise(eq(AllRequiredSubjectSubmittedEvent.of(memberId))));
      }
    }
  }
}
