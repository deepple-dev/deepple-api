package kr.co.deepple.datingexam.application.dto;

import java.util.List;
import java.util.Set;
import kr.co.deepple.datingexam.domain.DatingExamSubmit;

public record DatingExamInfoWithSubjectSubmissionResponse(
    List<DatingExamSubjectInfoWithSubjectSubmission> subjects) {
  public DatingExamInfoWithSubjectSubmissionResponse(
      DatingExamInfoResponse datingExamInfoResponse, Set<DatingExamSubmit> submittedSubjectInfo) {
    this(
        datingExamInfoResponse.subjects().stream()
            .map(subjectInfo ->
                new DatingExamSubjectInfoWithSubjectSubmission(subjectInfo, submittedSubjectInfo))
            .toList());
  }
}
