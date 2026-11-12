package kr.co.deepple.datingexam.application.dto;

import kr.co.deepple.datingexam.domain.DatingExamSubmit;

import java.util.List;
import java.util.Set;

public record DatingExamInfoWithSubjectSubmissionResponse(
    List<DatingExamSubjectInfoWithSubjectSubmission> subjects
) {
    public DatingExamInfoWithSubjectSubmissionResponse(
        DatingExamInfoResponse datingExamInfoResponse,
        Set<DatingExamSubmit> submittedSubjectInfo
    ) {
        this(
            datingExamInfoResponse.subjects().stream()
                .map(subjectInfo ->
                    new DatingExamSubjectInfoWithSubjectSubmission(
                        subjectInfo,
                        submittedSubjectInfo
                    )
                )
                .toList()
        );
    }
}
