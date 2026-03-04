package deepple.deepple.datingexam.application;

import deepple.deepple.common.event.Events;
import deepple.deepple.datingexam.application.dto.AllRequiredSubjectSubmittedEvent;
import deepple.deepple.datingexam.application.dto.DatingExamInfoResponse;
import deepple.deepple.datingexam.application.provided.DatingExamSubmitter;
import deepple.deepple.datingexam.application.required.*;
import deepple.deepple.datingexam.domain.*;
import deepple.deepple.datingexam.domain.dto.AnswerSubmitRequest;
import deepple.deepple.datingexam.domain.dto.DatingExamSubmitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        DatingExamSubmit datingExamSubmit = DatingExamSubmit.from(submitRequest, answerEncoder, memberId);
        datingExamSubmitRepository.save(datingExamSubmit);
        updateSubmitResult(submitRequest, memberId);
        checkAndPublishAllRequiredSubjectsSubmitted(subject, memberId);
    }

    private void validateSubmit(DatingExamSubmitRequest submitRequest, DatingExamSubject subject, Long memberId) {
        if (datingExamSubmitRepository.existsByMemberIdAndSubjectId(memberId, submitRequest.subjectId())) {
            throw new IllegalStateException("이미 제출한 과목입니다.");
        }
        DatingExamInfoResponse validDatingExamInfo = datingExamQueryRepository
            .findDatingExamInfo(subject.getType());
        DatingExamSubmitRequestValidator.validateSubmit(submitRequest, validDatingExamInfo);
    }

    private DatingExamSubject getDatingExamSubject(Long subjectId) {
        return datingExamSubjectRepository.findById(subjectId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과목입니다. subjectId: " + subjectId));
    }

    private void checkAndPublishAllRequiredSubjectsSubmitted(DatingExamSubject subject, Long memberId) {
        if (subject.isRequired() == false) {
            return;
        }

        if (isAllRequiredSubjectsSubmitted(memberId)) {
            Events.raise(AllRequiredSubjectSubmittedEvent.of(memberId));
        }
    }

    private void updateSubmitResult(DatingExamSubmitRequest submitRequest, long memberId) {
        List<Long> answerIds = submitRequest.answers().stream()
            .map(AnswerSubmitRequest::answerId)
            .toList();

        List<DatingExamAnswer> answers = datingExamAnswerRepository.findAllByIdIn(answerIds);

        Map<AnswerPersonalityType, Integer> counts = answers.stream()
            .collect(Collectors.groupingBy(
                DatingExamAnswer::getPersonalityType,
                Collectors.summingInt(e -> 1)
            ));

        DatingExamSubmitResult result = datingExamSubmitResultRepository.findByMemberIdForUpdate(memberId)
            .orElseGet(() -> DatingExamSubmitResult.create(memberId));
        result.addCounts(counts);
        datingExamSubmitResultRepository.save(result);
    }

    private boolean isAllRequiredSubjectsSubmitted(Long memberId) {
        Set<DatingExamSubject> requiredSubjects = datingExamSubjectRepository.findAllByType(
            SubjectType.REQUIRED);
        Set<DatingExamSubmit> submits = datingExamSubmitRepository.findAllByMemberId(memberId);

        return requiredSubjects.stream()
            .allMatch(requiredSubject -> submits.stream()
                .anyMatch(submit -> submit.getSubjectId().equals(requiredSubject.getId())));
    }
}
