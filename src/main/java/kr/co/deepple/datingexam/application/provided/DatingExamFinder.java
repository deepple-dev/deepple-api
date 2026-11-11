package kr.co.deepple.datingexam.application.provided;

import kr.co.deepple.datingexam.application.dto.DatingExamInfoWithSubjectSubmissionResponse;
import kr.co.deepple.datingexam.application.dto.DominantPersonalityTypeResponse;

public interface DatingExamFinder {
    DatingExamInfoWithSubjectSubmissionResponse findRequiredExamInfo(Long memberId);

    DominantPersonalityTypeResponse findDominantPersonalityType(Long memberId);
}
