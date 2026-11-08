package deepple.deepple.interview.command.application.answer;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.interview.command.domain.InterviewErrorCode;
import deepple.deepple.interview.command.domain.answer.InterviewAnswer;
import deepple.deepple.interview.command.domain.answer.InterviewAnswerCommandRepository;
import deepple.deepple.interview.command.domain.question.InterviewQuestion;
import deepple.deepple.interview.command.domain.question.InterviewQuestionCommandRepository;
import deepple.deepple.interview.fixture.InterviewAnswerFixture;
import deepple.deepple.interview.fixture.InterviewQuestionFixture;
import deepple.deepple.interview.presentation.answer.dto.InterviewAnswerSaveRequest;
import deepple.deepple.interview.presentation.answer.dto.InterviewAnswerUpdateRequest;
import deepple.deepple.mission.command.application.memberMission.MemberMissionService;
import deepple.deepple.mission.command.domain.mission.ActionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewAnswerServiceTest {
    @Mock
    private InterviewQuestionCommandRepository interviewQuestionCommandRepository;

    @Mock
    private InterviewAnswerCommandRepository interviewAnswerCommandRepository;

    @Mock
    private MemberMissionService memberMissionService;

    @InjectMocks
    private InterviewAnswerService interviewAnswerService;

    @Nested
    @DisplayName("saveAnswer 메서드 테스트")
    class SaveAnswerMethodTest {
        @Test
        @DisplayName("인터뷰 질문이 존재하지 않으면 예외를 던진다.")
        void throwsExceptionWhenInterviewQuestionDoesNotExist() {
            // given
            Long interviewQuestionId = 1L;
            String answerContent = "content";
            InterviewAnswerSaveRequest request = new InterviewAnswerSaveRequest(interviewQuestionId, answerContent);
            Long memberId = 2L;

            when(interviewQuestionCommandRepository.findById(interviewQuestionId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewAnswerService.saveAnswer(request, memberId))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND);
        }

        @Test
        @DisplayName("인터뷰 질문이 공개되어 있지 않다면 예외를 던진다.")
        void throwsExceptionWhenInterviewQuestionIsNotPublic() {
            // given
            Long interviewQuestionId = 1L;
            String answerContent = "content";
            InterviewAnswerSaveRequest request = new InterviewAnswerSaveRequest(interviewQuestionId, answerContent);
            Long memberId = 2L;

            InterviewQuestion interviewQuestion = InterviewQuestionFixture.builder()
                .id(interviewQuestionId)
                .publicQuestion(false)
                .build();
            when(interviewQuestionCommandRepository.findById(interviewQuestionId)).thenReturn(
                Optional.of(interviewQuestion));

            // when & then
            assertThatThrownBy(() -> interviewAnswerService.saveAnswer(request, memberId))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", InterviewErrorCode.INTERVIEW_QUESTION_NOT_PUBLIC);
        }

        @Test
        @DisplayName("인터뷰 답변이 이미 존재한다면 예외를 던진다.")
        void throwsExceptionWhenInterviewAnswerAlreadyExists() {
            // given
            Long interviewQuestionId = 1L;
            String answerContent = "content";
            InterviewAnswerSaveRequest request = new InterviewAnswerSaveRequest(interviewQuestionId, answerContent);
            Long memberId = 2L;

            InterviewQuestion interviewQuestion = InterviewQuestionFixture.builder()
                .id(interviewQuestionId)
                .publicQuestion(true)
                .build();
            when(interviewQuestionCommandRepository.findById(interviewQuestionId)).thenReturn(
                Optional.of(interviewQuestion));
            when(interviewAnswerCommandRepository.existsByQuestionIdAndMemberId(interviewQuestionId,
                memberId)).thenReturn(
                true);

            // when & then
            assertThatThrownBy(() -> interviewAnswerService.saveAnswer(request, memberId))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", InterviewErrorCode.INTERVIEW_ANSWER_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("다른 인터뷰 답변이 존재하지 않으면 미션 서비스를 호출한다.")
        void callsMemberMissionServiceWhenNoOtherInterviewAnswerExists() {
            // given
            Long interviewQuestionId = 1L;
            String answerContent = "content";
            InterviewAnswerSaveRequest request = new InterviewAnswerSaveRequest(interviewQuestionId, answerContent);
            Long memberId = 2L;

            InterviewQuestion interviewQuestion = InterviewQuestionFixture.builder()
                .id(interviewQuestionId)
                .publicQuestion(true)
                .build();
            when(interviewQuestionCommandRepository.findById(interviewQuestionId)).thenReturn(
                Optional.of(interviewQuestion));
            when(interviewAnswerCommandRepository.existsByMemberId(memberId)).thenReturn(false);
            when(interviewAnswerCommandRepository.existsByQuestionIdAndMemberId(interviewQuestionId,
                memberId)).thenReturn(
                false);

            when(memberMissionService.executeMissionsByAction(memberId, ActionType.INTERVIEW.name()))
                .thenReturn(true);

            InterviewAnswer interviewAnswer = mock(InterviewAnswer.class);
            try (MockedStatic<InterviewAnswer> mockedStatic = mockStatic(InterviewAnswer.class)) {
                mockedStatic.when(() -> InterviewAnswer.of(eq(interviewQuestionId), eq(memberId), eq(answerContent)))
                    .thenReturn(interviewAnswer);

                // when
                boolean result = interviewAnswerService.saveAnswer(request, memberId);

                // then
                assertThat(result).isTrue();
            }
            verify(interviewAnswerCommandRepository).save(interviewAnswer);
            verify(memberMissionService).executeMissionsByAction(memberId, ActionType.INTERVIEW.name());
        }

        @Test
        @DisplayName("다른 인터뷰 답변이 존재하면 MemberMissionService를 호출하지 않는다.")
        void doesNotCallMemberMissionServiceWhenOtherInterviewAnswerExists() {
            // given
            Long interviewQuestionId = 1L;
            String answerContent = "content";
            InterviewAnswerSaveRequest request = new InterviewAnswerSaveRequest(interviewQuestionId, answerContent);
            Long memberId = 2L;

            InterviewQuestion interviewQuestion = InterviewQuestionFixture.builder()
                .id(interviewQuestionId)
                .publicQuestion(true)
                .build();
            when(interviewQuestionCommandRepository.findById(interviewQuestionId)).thenReturn(
                Optional.of(interviewQuestion));
            when(interviewAnswerCommandRepository.existsByMemberId(memberId)).thenReturn(true);
            when(interviewAnswerCommandRepository.existsByQuestionIdAndMemberId(interviewQuestionId,
                memberId)).thenReturn(
                false);

            InterviewAnswer interviewAnswer = mock(InterviewAnswer.class);
            try (MockedStatic<InterviewAnswer> mockedStatic = mockStatic(InterviewAnswer.class)) {
                mockedStatic.when(() -> InterviewAnswer.of(eq(interviewQuestionId), eq(memberId), eq(answerContent)))
                    .thenReturn(interviewAnswer);

                // when
                boolean result = interviewAnswerService.saveAnswer(request, memberId);

                // then
                assertThat(result).isFalse();
            }
            verify(interviewAnswerCommandRepository).save(interviewAnswer);
            verify(memberMissionService, never()).executeMissionsByAction(anyLong(), anyString());
        }
    }

    @Nested
    @DisplayName("updateAnswer 메서드 테스트")
    class UpdateAnswerMethodTest {
        @Test
        @DisplayName("인터뷰 답변이 존재하지 않으면 예외를 던진다.")
        void throwsExceptionWhenInterviewAnswerDoesNotExist() {
            // given
            Long answerId = 1L;
            String answerContent = "content";
            InterviewAnswerUpdateRequest request = new InterviewAnswerUpdateRequest(answerContent);
            Long memberId = 2L;

            when(interviewAnswerCommandRepository.findById(answerId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewAnswerService.updateAnswer(answerId, request, memberId))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", InterviewErrorCode.INTERVIEW_ANSWER_NOT_FOUND);
        }

        @Test
        @DisplayName("인터뷰 답변이 본인이 작성한 것이 아니라면 예외를 던진다.")
        void throwsExceptionWhenInterviewAnswerIsNotWrittenByMember() {
            // given
            Long answerId = 1L;
            String answerContent = "content";
            InterviewAnswerUpdateRequest request = new InterviewAnswerUpdateRequest(answerContent);
            Long memberId = 2L;

            InterviewAnswer interviewAnswer = InterviewAnswerFixture.builder()
                .id(answerId)
                .memberId(3L)
                .build();
            when(interviewAnswerCommandRepository.findById(answerId)).thenReturn(Optional.of(interviewAnswer));

            // when & then
            assertThatThrownBy(() -> interviewAnswerService.updateAnswer(answerId, request, memberId))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", InterviewErrorCode.INTERVIEW_ANSWER_ACCESS_DENIED);
        }

        @Test
        @DisplayName("인터뷰 답변을 업데이트 한다.")
        void updatesInterviewAnswer() {
            // given
            Long answerId = 1L;
            String answerContent = "content";
            InterviewAnswerUpdateRequest request = new InterviewAnswerUpdateRequest(answerContent);
            Long memberId = 2L;

            InterviewAnswer interviewAnswer = InterviewAnswerFixture.builder()
                .id(answerId)
                .memberId(memberId)
                .build();
            when(interviewAnswerCommandRepository.findById(answerId)).thenReturn(Optional.of(interviewAnswer));

            // when
            interviewAnswerService.updateAnswer(answerId, request, memberId);

            // then
            assertThat(interviewAnswer.getContent()).isEqualTo(answerContent);
        }
    }

    @Nested
    @DisplayName("deleteAnswer 메서드 테스트")
    class DeleteAnswerMethodTest {
        @Test
        @DisplayName("인터뷰 답변이 존재하지 않으면 예외를 던진다.")
        void throwsExceptionWhenInterviewAnswerDoesNotExist() {
            // given
            Long answerId = 1L;
            Long memberId = 2L;

            when(interviewAnswerCommandRepository.findById(answerId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewAnswerService.deleteAnswer(answerId, memberId))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", InterviewErrorCode.INTERVIEW_ANSWER_NOT_FOUND);
        }

        @Test
        @DisplayName("인터뷰 답변이 본인이 작성한 것이 아니라면 예외를 던진다.")
        void throwsExceptionWhenInterviewAnswerIsNotWrittenByMember() {
            // given
            Long answerId = 1L;
            Long memberId = 2L;

            InterviewAnswer interviewAnswer = InterviewAnswerFixture.builder()
                .id(answerId)
                .memberId(3L)
                .build();
            when(interviewAnswerCommandRepository.findById(answerId)).thenReturn(Optional.of(interviewAnswer));

            // when & then
            assertThatThrownBy(() -> interviewAnswerService.deleteAnswer(answerId, memberId))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", InterviewErrorCode.INTERVIEW_ANSWER_ACCESS_DENIED);
        }

        @Test
        @DisplayName("인터뷰 답변을 삭제한다.")
        void deletesInterviewAnswer() {
            // given
            Long answerId = 1L;
            Long memberId = 2L;

            InterviewAnswer interviewAnswer = InterviewAnswerFixture.builder()
                .id(answerId)
                .memberId(memberId)
                .build();
            when(interviewAnswerCommandRepository.findById(answerId)).thenReturn(Optional.of(interviewAnswer));

            // when
            interviewAnswerService.deleteAnswer(answerId, memberId);

            // then
            verify(interviewAnswerCommandRepository, times(1)).delete(interviewAnswer);
        }
    }
}
