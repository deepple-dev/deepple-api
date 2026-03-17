package deepple.deepple.datingexam.application.provided;

import deepple.deepple.datingexam.application.dto.DatingExamInfoWithSubjectSubmissionResponse;
import deepple.deepple.datingexam.application.dto.DominantPersonalityTypeResponse;

public interface DatingExamFinder {
    DatingExamInfoWithSubjectSubmissionResponse findRequiredExamInfo(Long memberId);

    DatingExamInfoWithSubjectSubmissionResponse findOptionalExamInfo(Long memberId);

    DominantPersonalityTypeResponse findDominantPersonalityType(Long memberId);
}
