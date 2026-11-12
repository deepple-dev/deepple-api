package kr.co.deepple.datingexam.application;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.common.event.Events;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.application.dto.AllRequiredSubjectSubmittedEvent;
import kr.co.deepple.datingexam.application.dto.DatingExamInfoResponse;
import kr.co.deepple.datingexam.application.port.in.DatingExamSubmitter;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class DatingExamModifyService implements DatingExamSubmitter {

  private final DatingExamSubjectRepository datingExamSubjectRepository;
  private final DatingExamSubmitRepository datingExamSubmitRepository;
  private final DatingExamQueryRepository datingExamQueryRepository;
  private final DatingExamAnswerEncoder answerEncoder;
  private final DatingExamAnswerRepository datingExamAnswerRepository;
  private final DatingExamSubmitResultRepository datingExamSubmitResultRepository;

  @Override
  @Transactional
  public void submitSubject(DatingExamSubmitRequest submitRequest, long memberId) {
    DatingExamSubject subject = getDatingExamSubject(submitRequest.subjectId());
    validateSubmit(submitRequest, subject, memberId);
    DatingExamSubmit datingExamSubmit =
        DatingExamSubmit.from(submitRequest, answerEncoder, memberId);
    datingExamSubmitRepository.save(datingExamSubmit);
    updateSubmitResult(submitRequest, memberId);
    checkAndPublishAllRequiredSubjectsSubmitted(subject, memberId);
  }

  private void validateSubmit(
      DatingExamSubmitRequest submitRequest, DatingExamSubject subject, Long memberId) {
    if (datingExamSubmitRepository.existsByMemberIdAndSubjectId(
        memberId, submitRequest.subjectId())) {
      throw new DeeppleException(
          DatingExamErrorCode.DATING_EXAM_ALREADY_SUBMITTED,
          "memberId=" + memberId + ", subjectId=" + submitRequest.subjectId());
    }
    DatingExamInfoResponse validDatingExamInfo =
        datingExamQueryRepository.findDatingExamInfo(subject.getType());
    DatingExamSubmitRequestValidator.validateSubmit(submitRequest, validDatingExamInfo);
  }

  private DatingExamSubject getDatingExamSubject(Long subjectId) {
    return datingExamSubjectRepository
        .findById(subjectId)
        .orElseThrow(() ->
            new DeeppleException(
                DatingExamErrorCode.DATING_EXAM_SUBJECT_NOT_FOUND, "subjectId=" + subjectId));
  }

  private void checkAndPublishAllRequiredSubjectsSubmitted(
      DatingExamSubject subject, Long memberId) {
    if (subject.isRequired() == false) {
      return;
    }

    if (isAllRequiredSubjectsSubmitted(memberId)) {
      Events.raise(AllRequiredSubjectSubmittedEvent.of(memberId));
    }
  }

  private void updateSubmitResult(DatingExamSubmitRequest submitRequest, long memberId) {
    List<Long> answerIds =
        submitRequest.answers().stream().map(AnswerSubmitRequest::answerId).toList();

    List<DatingExamAnswer> answers = datingExamAnswerRepository.findAllByIdIn(answerIds);

    Map<AnswerPersonalityType, Integer> counts =
        answers.stream()
            .collect(
                Collectors.groupingBy(
                    DatingExamAnswer::getPersonalityType, Collectors.summingInt(e -> 1)));

    DatingExamSubmitResult result =
        datingExamSubmitResultRepository
            .findByMemberIdForUpdate(memberId)
            .orElseGet(() -> DatingExamSubmitResult.create(memberId));
    result.addCounts(counts);
    datingExamSubmitResultRepository.save(result);
  }

  private boolean isAllRequiredSubjectsSubmitted(Long memberId) {
    Set<DatingExamSubject> requiredSubjects =
        datingExamSubjectRepository.findAllByType(SubjectType.REQUIRED);
    Set<DatingExamSubmit> submits = datingExamSubmitRepository.findAllByMemberId(memberId);

    return requiredSubjects.stream()
        .allMatch(requiredSubject ->
            submits.stream()
                .anyMatch(submit -> submit.getSubjectId().equals(requiredSubject.getId())));
  }
}
