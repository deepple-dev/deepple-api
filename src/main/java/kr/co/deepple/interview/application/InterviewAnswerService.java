package kr.co.deepple.interview.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.interview.application.dto.InterviewAnswerSaveRequest;
import kr.co.deepple.interview.application.dto.InterviewAnswerUpdateRequest;
import kr.co.deepple.interview.application.port.out.InterviewAnswerCommandRepository;
import kr.co.deepple.interview.application.port.out.InterviewQuestionCommandRepository;
import kr.co.deepple.interview.domain.InterviewAnswer;
import kr.co.deepple.interview.domain.InterviewErrorCode;
import kr.co.deepple.interview.domain.InterviewQuestion;
import kr.co.deepple.mission.application.port.in.ExecuteMissionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewAnswerService {

  private static final String INTERVIEW_MISSION_ACTION = "INTERVIEW";

  private final InterviewAnswerCommandRepository interviewAnswerCommandRepository;
  private final InterviewQuestionCommandRepository interviewQuestionCommandRepository;
  private final ExecuteMissionUseCase executeMissionUseCase;

  @Transactional
  public boolean saveAnswer(InterviewAnswerSaveRequest request, long memberId) {
    validateQuestion(request.interviewQuestionId(), memberId);
    boolean isFirstInterviewAnswer = isFirstInterviewAnswer(memberId);
    createInterviewAnswer(request.interviewQuestionId(), memberId, request.answerContent());
    boolean hasProcessedMission = false;
    if (isFirstInterviewAnswer) {
      hasProcessedMission =
          executeMissionUseCase.executeMissionsByAction(memberId, INTERVIEW_MISSION_ACTION);
    }
    return hasProcessedMission;
  }

  @Transactional
  public void updateAnswer(long answerId, InterviewAnswerUpdateRequest request, long memberId) {
    InterviewAnswer interviewAnswer = getInterviewAnswer(answerId);
    validateAnswer(interviewAnswer, memberId);
    interviewAnswer.updateContent(request.answerContent());
  }

  @Transactional
  public void deleteAnswer(long answerId, long memberId) {
    InterviewAnswer interviewAnswer = getInterviewAnswer(answerId);
    validateAnswer(interviewAnswer, memberId);
    interviewAnswerCommandRepository.delete(interviewAnswer);
  }

  private void validateQuestion(long questionId, long memberId) {
    InterviewQuestion interviewQuestion =
        interviewQuestionCommandRepository
            .findById(questionId)
            .orElseThrow(() ->
                new DeeppleException(
                    InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND, "questionId=" + questionId));
    if (!interviewQuestion.isPublic()) {
      throw new DeeppleException(
          InterviewErrorCode.INTERVIEW_QUESTION_NOT_PUBLIC, "questionId=" + questionId);
    }
    if (interviewAnswerCommandRepository.existsByQuestionIdAndMemberId(questionId, memberId)) {
      throw new DeeppleException(
          InterviewErrorCode.INTERVIEW_ANSWER_ALREADY_EXISTS,
          "questionId=" + questionId + ", memberId=" + memberId);
    }
  }

  private InterviewAnswer createInterviewAnswer(long questionId, long memberId, String content) {
    InterviewAnswer interviewAnswer = InterviewAnswer.of(questionId, memberId, content);
    return interviewAnswerCommandRepository.save(interviewAnswer);
  }

  private boolean isFirstInterviewAnswer(long memberId) {
    return !interviewAnswerCommandRepository.existsByMemberId(memberId);
  }

  private InterviewAnswer getInterviewAnswer(long id) {
    return interviewAnswerCommandRepository
        .findById(id)
        .orElseThrow(() ->
            new DeeppleException(InterviewErrorCode.INTERVIEW_ANSWER_NOT_FOUND, "answerId=" + id));
  }

  private void validateAnswer(InterviewAnswer interviewAnswer, long memberId) {
    if (!interviewAnswer.isAnsweredBy(memberId)) {
      throw new DeeppleException(
          InterviewErrorCode.INTERVIEW_ANSWER_ACCESS_DENIED,
          "answerId=" + interviewAnswer.getId() + ", memberId=" + memberId);
    }
  }
}
