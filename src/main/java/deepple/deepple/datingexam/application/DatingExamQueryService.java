package deepple.deepple.datingexam.application;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.datingexam.application.dto.DatingExamInfoResponse;
import deepple.deepple.datingexam.application.dto.DatingExamInfoWithSubjectSubmissionResponse;
import deepple.deepple.datingexam.application.dto.DominantPersonalityTypeResponse;
import deepple.deepple.datingexam.application.provided.DatingExamFinder;
import deepple.deepple.datingexam.application.required.DatingExamQueryRepository;
import deepple.deepple.datingexam.application.required.DatingExamSubmitRepository;
import deepple.deepple.datingexam.application.required.DatingExamSubmitResultRepository;
import deepple.deepple.datingexam.domain.DatingExamErrorCode;
import deepple.deepple.datingexam.domain.DatingExamSubmit;
import deepple.deepple.datingexam.domain.DatingExamSubmitResult;
import deepple.deepple.datingexam.domain.SubjectType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class DatingExamQueryService implements DatingExamFinder {
    private final DatingExamSubmitRepository datingExamSubmitRepository;
    private final DatingExamQueryRepository datingExamQueryRepository;
    private final DatingExamSubmitResultRepository datingExamSubmitResultRepository;

    @Override
    public DatingExamInfoWithSubjectSubmissionResponse findRequiredExamInfo(Long memberId) {
        DatingExamInfoResponse datingExamInfo = datingExamQueryRepository.findDatingExamInfo(SubjectType.REQUIRED);
        Set<DatingExamSubmit> submittedExams = datingExamSubmitRepository.findAllByMemberId(memberId);
        return new DatingExamInfoWithSubjectSubmissionResponse(datingExamInfo, submittedExams);
    }

    @Override
    public DominantPersonalityTypeResponse findDominantPersonalityType(Long memberId) {
        DatingExamSubmitResult result = datingExamSubmitResultRepository.findByMemberId(memberId)
            .orElseThrow(() -> new DeeppleException(
                DatingExamErrorCode.DATING_EXAM_RESULT_NOT_FOUND,
                "memberId=" + memberId
            ));
        return DominantPersonalityTypeResponse.from(result);
    }
}
