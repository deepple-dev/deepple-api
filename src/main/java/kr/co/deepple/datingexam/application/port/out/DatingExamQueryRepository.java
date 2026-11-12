package kr.co.deepple.datingexam.application.port.out;

import kr.co.deepple.datingexam.application.dto.DatingExamInfoResponse;
import kr.co.deepple.datingexam.domain.SubjectType;

public interface DatingExamQueryRepository {
    DatingExamInfoResponse findDatingExamInfo(SubjectType subjectType);
}
