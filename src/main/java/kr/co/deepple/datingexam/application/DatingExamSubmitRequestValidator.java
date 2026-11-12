package kr.co.deepple.datingexam.application;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.application.dto.DatingExamAnswerInfo;
import kr.co.deepple.datingexam.application.dto.DatingExamInfoResponse;
import kr.co.deepple.datingexam.application.dto.DatingExamQuestionInfo;
import kr.co.deepple.datingexam.application.dto.DatingExamSubjectInfo;
import kr.co.deepple.datingexam.domain.DatingExamErrorCode;
import kr.co.deepple.datingexam.domain.dto.AnswerSubmitRequest;
import kr.co.deepple.datingexam.domain.dto.DatingExamSubmitRequest;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DatingExamSubmitRequestValidator {

  public static void validateSubmit(DatingExamSubmitRequest request, DatingExamInfoResponse info) {
    Map<Long, Map<Long, Set<Long>>> validMap = toValidMap(info);
    validateSubject(request, validMap);
  }

  private static Map<Long, Map<Long, Set<Long>>> toValidMap(DatingExamInfoResponse info) {
    return info.subjects().stream()
        .collect(
            Collectors.toMap(
                DatingExamSubjectInfo::id,
                subjectInfo ->
                    subjectInfo.questions().stream()
                        .collect(
                            Collectors.toMap(
                                DatingExamQuestionInfo::id,
                                questionInfo ->
                                    questionInfo.answers().stream()
                                        .map(DatingExamAnswerInfo::id)
                                        .collect(Collectors.toSet())))));
  }

  private static void validateSubject(
      DatingExamSubmitRequest request, Map<Long, Map<Long, Set<Long>>> validMap) {
    Long subjectId = request.subjectId();
    Map<Long, Set<Long>> questionsMap = validMap.get(subjectId);
    if (questionsMap == null) {
      throw new DeeppleException(
          DatingExamErrorCode.INVALID_DATING_EXAM_SUBMIT, "존재하지 않는 subjectId 입니다: " + subjectId);
    }
    validateQuestion(request, questionsMap);
  }

  private static void validateQuestion(
      DatingExamSubmitRequest subjectRequest, Map<Long, Set<Long>> questionsMap) {
    boolean hasDuplicates =
        subjectRequest.answers().stream().map(AnswerSubmitRequest::questionId).distinct().count()
            < subjectRequest.answers().size();

    if (hasDuplicates) {
      throw new DeeppleException(
          DatingExamErrorCode.INVALID_DATING_EXAM_SUBMIT,
          "subjectId=" + subjectRequest.subjectId() + " 에 중복된 questionId가 존재합니다.");
    }

    if (subjectRequest.answers().size() != questionsMap.size()) {
      throw new DeeppleException(
          DatingExamErrorCode.INVALID_DATING_EXAM_SUBMIT,
          "subjectId=" + subjectRequest.subjectId() + " 에 속하는 questionId의 수가 올바르지 않습니다.");
    }

    for (AnswerSubmitRequest answerRequest : subjectRequest.answers()) {
      Long questionId = answerRequest.questionId();
      Set<Long> validAnswers = questionsMap.get(questionId);
      if (validAnswers == null) {
        throw new DeeppleException(
            DatingExamErrorCode.INVALID_DATING_EXAM_SUBMIT,
            "subjectId=" + subjectRequest.subjectId() + " 에 속하지 않은 questionId 입니다: " + questionId);
      }
      validateAnswer(answerRequest, validAnswers);
    }
  }

  private static void validateAnswer(AnswerSubmitRequest answerRequest, Set<Long> validAnswers) {
    Long answerId = answerRequest.answerId();
    if (!validAnswers.contains(answerId)) {
      throw new DeeppleException(
          DatingExamErrorCode.INVALID_DATING_EXAM_SUBMIT,
          "questionId=" + answerRequest.questionId() + " 에 속하지 않은 answerId 입니다: " + answerId);
    }
  }
}
