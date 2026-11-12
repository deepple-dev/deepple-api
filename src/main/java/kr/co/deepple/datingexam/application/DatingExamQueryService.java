package kr.co.deepple.datingexam.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.application.dto.DatingExamInfoResponse;
import kr.co.deepple.datingexam.application.dto.DatingExamInfoWithSubjectSubmissionResponse;
import kr.co.deepple.datingexam.application.dto.DominantPersonalityTypeResponse;
import kr.co.deepple.datingexam.application.port.in.DatingExamFinder;
import kr.co.deepple.datingexam.application.port.out.DatingExamQueryRepository;
import kr.co.deepple.datingexam.application.port.out.DatingExamSubmitRepository;
import kr.co.deepple.datingexam.application.port.out.DatingExamSubmitResultRepository;
import kr.co.deepple.datingexam.domain.DatingExamErrorCode;
import kr.co.deepple.datingexam.domain.DatingExamSubmit;
import kr.co.deepple.datingexam.domain.DatingExamSubmitResult;
import kr.co.deepple.datingexam.domain.SubjectType;
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
