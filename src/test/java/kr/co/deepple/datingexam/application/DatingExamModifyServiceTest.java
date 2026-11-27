package kr.co.deepple.datingexam.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.application.dto.DatingExamInfoResponse;
import kr.co.deepple.datingexam.application.dto.DatingExamSubmitResponse;
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
import kr.co.deepple.datingexam.event.AllRequiredSubjectSubmittedEvent;
import kr.co.deepple.datingexam.event.DominantPersonalityTypeChangedEvent;
import kr.co.deepple.datingexam.fixture.DatingExamAnswerFixture;
import kr.co.deepple.datingexam.fixture.DatingExamSubjectFixture;
import kr.co.deepple.datingexam.fixture.DatingExamSubmitFixture;
import kr.co.deepple.datingexam.fixture.DatingExamSubmitResultFixture;
import kr.co.deepple.mission.application.port.in.FindMissionRewardUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class DatingExamModifyServiceTest {

  @Mock private ApplicationEventPublisher eventPublisher;

  @Mock private FindMissionRewardUseCase findMissionRewardUseCase;

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
            .when(() -> DatingExamSubmit.of(request, answerEncoder, memberId))
            .thenReturn(submit);
        mockedValidator
            .when(() -> DatingExamSubmitRequestValidator.validateSubmit(request, infoResponse))
            .thenAnswer(invocation -> null);

        // When
        DatingExamSubmitResponse response =
            datingExamModifyService.submitSubject(request, memberId);

        // Then
        mockedDatingExamSubmit.verify(() -> DatingExamSubmit.of(request, answerEncoder, memberId));
        verify(datingExamSubmitRepository).save(submit);
        verify(datingExamSubmitResultRepository).save(any(DatingExamSubmitResult.class));
        assertThat(response.rewardedHeartAmount()).isNull();
        verifyNoInteractions(findMissionRewardUseCase);
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
    @DisplayName("필수 과목 제출 후 모든 필수 과목이 제출된 경우 이벤트를 발행하고 받은 하트 개수를 응답한다.")
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
              mockStatic(DatingExamSubmitRequestValidator.class)) {

        DatingExamSubmit submit =
            DatingExamSubmitFixture.builder().memberId(memberId).subjectId(subjectId).build();
        mockedDatingExamSubmit
            .when(() -> DatingExamSubmit.of(request, answerEncoder, memberId))
            .thenReturn(submit);
        mockedValidator
            .when(() -> DatingExamSubmitRequestValidator.validateSubmit(request, infoResponse))
            .thenAnswer(invocation -> null);
        when(datingExamSubjectRepository.findAllByType(SubjectType.REQUIRED))
            .thenReturn(Set.of(subject));

        when(datingExamSubmitRepository.findAllByMemberId(memberId)).thenReturn(Set.of(submit));
        when(findMissionRewardUseCase.findNextRewardedHeartAmount(memberId, "FIRST_DATE_EXAM"))
            .thenReturn(15);

        // When
        DatingExamSubmitResponse response =
            datingExamModifyService.submitSubject(request, memberId);

        // Then
        mockedDatingExamSubmit.verify(() -> DatingExamSubmit.of(request, answerEncoder, memberId));
        verify(datingExamSubmitRepository).save(submit);
        verify(datingExamSubmitResultRepository).save(any(DatingExamSubmitResult.class));
        Mockito.verify(eventPublisher)
            .publishEvent(
                ArgumentMatchers.<Object>argThat(event ->
                    event instanceof AllRequiredSubjectSubmittedEvent written
                        && written.memberId() == memberId));
        assertThat(response.rewardedHeartAmount()).isEqualTo(15);
      }
    }
  }

  @Nested
  @DisplayName("대표 유형 변경 이벤트 테스트")
  class DominantPersonalityTypeChangedEventTests {

    private static final long MEMBER_ID = 1L;
    private static final long SUBJECT_ID = 3L;
    private static final long ANSWER_ID = 10L;

    @Test
    @DisplayName("처음 제출해 결과가 생기면 대표 유형을 담아 발행한다.")
    void publishWhenResultCreated() {
      // When
      submitAnswer(AnswerPersonalityType.DECISIVE_INDEPENDENT, Optional.empty());

      // Then
      verifyDominantEvent("DECISIVE_INDEPENDENT");
    }

    @Test
    @DisplayName("대표 유형이 그대로여도 제출할 때마다 현재 대표 유형을 담아 발행한다.")
    void publishCurrentDominantOnEverySubmit() {
      // Given
      DatingExamSubmitResult result =
          DatingExamSubmitResultFixture.builder()
              .memberId(MEMBER_ID)
              .counts(Map.of(AnswerPersonalityType.DECISIVE_INDEPENDENT, 2))
              .build();

      // When
      submitAnswer(AnswerPersonalityType.STIMULATING_ADVENTURER, Optional.of(result));

      // Then
      verifyDominantEvent("DECISIVE_INDEPENDENT");
    }

    @Test
    @DisplayName("제출로 대표 유형이 바뀌면 바뀐 대표 유형을 담아 발행한다.")
    void publishChangedDominant() {
      // Given
      DatingExamSubmitResult result =
          DatingExamSubmitResultFixture.builder().memberId(MEMBER_ID).build();

      // When
      submitAnswer(AnswerPersonalityType.STIMULATING_ADVENTURER, Optional.of(result));

      // Then
      verifyDominantEvent("STIMULATING_ADVENTURER");
    }

    private void submitAnswer(
        AnswerPersonalityType personalityType, Optional<DatingExamSubmitResult> result) {
      DatingExamSubmitRequest request = mock(DatingExamSubmitRequest.class);
      when(request.subjectId()).thenReturn(SUBJECT_ID);
      when(request.answers()).thenReturn(List.of(new AnswerSubmitRequest(1L, ANSWER_ID)));

      DatingExamSubject subject =
          DatingExamSubjectFixture.builder().id(SUBJECT_ID).type(SubjectType.OPTIONAL).build();
      when(datingExamSubjectRepository.findById(SUBJECT_ID)).thenReturn(Optional.of(subject));
      DatingExamInfoResponse infoResponse = mock(DatingExamInfoResponse.class);
      when(datingExamQueryRepository.findDatingExamInfo(SubjectType.OPTIONAL))
          .thenReturn(infoResponse);
      when(datingExamAnswerRepository.findAllByIdIn(List.of(ANSWER_ID)))
          .thenReturn(
              List.of(
                  DatingExamAnswerFixture.builder()
                      .id(ANSWER_ID)
                      .personalityType(personalityType)
                      .build()));
      when(datingExamSubmitResultRepository.findByMemberIdForUpdate(MEMBER_ID)).thenReturn(result);

      try (MockedStatic<DatingExamSubmit> mockedDatingExamSubmit =
              mockStatic(DatingExamSubmit.class);
          MockedStatic<DatingExamSubmitRequestValidator> mockedValidator =
              mockStatic(DatingExamSubmitRequestValidator.class)) {
        mockedDatingExamSubmit
            .when(() -> DatingExamSubmit.of(request, answerEncoder, MEMBER_ID))
            .thenReturn(
                DatingExamSubmitFixture.builder()
                    .memberId(MEMBER_ID)
                    .subjectId(SUBJECT_ID)
                    .build());
        mockedValidator
            .when(() -> DatingExamSubmitRequestValidator.validateSubmit(request, infoResponse))
            .thenAnswer(invocation -> null);

        datingExamModifyService.submitSubject(request, MEMBER_ID);
      }
    }

    private void verifyDominantEvent(String personalityType) {
      ArgumentCaptor<DominantPersonalityTypeChangedEvent> eventCaptor =
          ArgumentCaptor.forClass(DominantPersonalityTypeChangedEvent.class);
      verify(eventPublisher).publishEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().memberId()).isEqualTo(MEMBER_ID);
      assertThat(eventCaptor.getValue().personalityType()).isEqualTo(personalityType);
    }
  }
}
