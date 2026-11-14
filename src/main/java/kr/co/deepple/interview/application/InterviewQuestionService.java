package kr.co.deepple.interview.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.interview.application.dto.InterviewQuestionSaveRequest;
import kr.co.deepple.interview.application.port.out.InterviewQuestionRepository;
import kr.co.deepple.interview.domain.InterviewCategory;
import kr.co.deepple.interview.domain.InterviewErrorCode;
import kr.co.deepple.interview.domain.InterviewQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewQuestionService {

  private final InterviewQuestionRepository interviewQuestionRepository;

  @Transactional
  public void createQuestion(InterviewQuestionSaveRequest request) {
    validateQuestionCreation(request.questionContent());
    InterviewCategory interviewCategory = InterviewCategory.from(request.category());
    createQuestion(request.questionContent(), interviewCategory, request.isPublic());
  }

  @Transactional
  public void updateQuestion(long questionId, InterviewQuestionSaveRequest request) {
    InterviewQuestion interviewQuestion = getInterviewQuestion(questionId);
    InterviewCategory interviewCategory = InterviewCategory.from(request.category());
    validateQuestionUpdate(interviewQuestion, request.questionContent());
    interviewQuestion.update(request.questionContent(), interviewCategory, request.isPublic());
  }

  private void validateQuestionCreation(String questionContent) {
    if (interviewQuestionRepository.existsByContent(questionContent)) {
      throw new DeeppleException(
          InterviewErrorCode.INTERVIEW_QUESTION_ALREADY_EXISTS, "content=" + questionContent);
    }
  }

  private void createQuestion(
      String questionContent, InterviewCategory interviewCategory, boolean isPublic) {
    InterviewQuestion interviewQuestion =
        InterviewQuestion.of(questionContent, interviewCategory, isPublic);
    interviewQuestionRepository.save(interviewQuestion);
  }

  private InterviewQuestion getInterviewQuestion(long questionId) {
    return interviewQuestionRepository
        .findById(questionId)
        .orElseThrow(() ->
            new DeeppleException(
                InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND, "questionId=" + questionId));
  }

  private void validateQuestionUpdate(InterviewQuestion interviewQuestion, String questionContent) {
    if (interviewQuestion.getContent().equals(questionContent)) {
      return;
    }
    if (interviewQuestionRepository.existsByContent(questionContent)) {
      throw new DeeppleException(
          InterviewErrorCode.INTERVIEW_QUESTION_ALREADY_EXISTS, "content=" + questionContent);
    }
  }
}
