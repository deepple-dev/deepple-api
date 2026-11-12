package kr.co.deepple.datingexam.application.port.in;

import kr.co.deepple.datingexam.application.dto.DatingExamInfoWithSubjectSubmissionResponse;
import kr.co.deepple.datingexam.application.dto.DominantPersonalityTypeResponse;

public interface DatingExamFinder {
  DatingExamInfoWithSubjectSubmissionResponse findRequiredExamInfo(long memberId);

  DominantPersonalityTypeResponse findDominantPersonalityType(long memberId);
}
