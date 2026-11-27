package kr.co.deepple.datingexam.application;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.application.dto.DatingExamInfoResponse;
import kr.co.deepple.datingexam.application.dto.DatingExamSubmitResponse;
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
import kr.co.deepple.datingexam.event.AllRequiredSubjectSubmittedEvent;
import kr.co.deepple.datingexam.event.DominantPersonalityTypeChangedEvent;
import kr.co.deepple.mission.application.port.in.FindMissionRewardUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class DatingExamModifyService implements DatingExamSubmitter {

  private static final String FIRST_DATE_EXAM_MISSION_ACTION = "FIRST_DATE_EXAM";

  private final DatingExamSubjectRepository datingExamSubjectRepository;
  private final DatingExamSubmitRepository datingExamSubmitRepository;
  private final DatingExamQueryRepository datingExamQueryRepository;
  private final DatingExamAnswerEncoder answerEncoder;
  private final DatingExamAnswerRepository datingExamAnswerRepository;
  private final DatingExamSubmitResultRepository datingExamSubmitResultRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final FindMissionRewardUseCase findMissionRewardUseCase;

  @Override
  @Transactional
  public DatingExamSubmitResponse submitSubject(
      DatingExamSubmitRequest submitRequest, long memberId) {
    DatingExamSubject subject = getDatingExamSubject(submitRequest.subjectId());
    validateSubmit(submitRequest, subject, memberId);
    DatingExamSubmit datingExamSubmit = DatingExamSubmit.of(submitRequest, answerEncoder, memberId);
    datingExamSubmitRepository.save(datingExamSubmit);
    updateSubmitResult(submitRequest, memberId);
    return new DatingExamSubmitResponse(completeRequiredSubjects(subject, memberId));
  }

  private void validateSubmit(
      DatingExamSubmitRequest submitRequest, DatingExamSubject subject, long memberId) {
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

  private DatingExamSubject getDatingExamSubject(long subjectId) {
    return datingExamSubjectRepository
        .findById(subjectId)
        .orElseThrow(() ->
            new DeeppleException(
                DatingExamErrorCode.DATING_EXAM_SUBJECT_NOT_FOUND, "subjectId=" + subjectId));
  }

  private Integer completeRequiredSubjects(DatingExamSubject subject, long memberId) {
    if (!subject.isRequired() || !isAllRequiredSubjectsSubmitted(memberId)) {
      return null;
    }
    Integer rewardedHeartAmount =
        findMissionRewardUseCase.findNextRewardedHeartAmount(
            memberId, FIRST_DATE_EXAM_MISSION_ACTION);
    eventPublisher.publishEvent(AllRequiredSubjectSubmittedEvent.from(memberId));
    return rewardedHeartAmount;
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
            .orElseGet(() -> DatingExamSubmitResult.from(memberId));
    result.addCounts(counts);
    datingExamSubmitResultRepository.save(result);
    eventPublisher.publishEvent(
        DominantPersonalityTypeChangedEvent.of(
            memberId, result.getDominantPersonalityType().name()));
  }

  private boolean isAllRequiredSubjectsSubmitted(long memberId) {
    Set<DatingExamSubject> requiredSubjects =
        datingExamSubjectRepository.findAllByType(SubjectType.REQUIRED);
    Set<DatingExamSubmit> submits = datingExamSubmitRepository.findAllByMemberId(memberId);

    return requiredSubjects.stream()
        .allMatch(requiredSubject ->
            submits.stream().anyMatch(submit -> submit.getSubjectId() == requiredSubject.getId()));
  }
}
