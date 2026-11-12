package kr.co.deepple.datingexam.application.dto;

import kr.co.deepple.datingexam.domain.DatingExamSubmit;
import kr.co.deepple.datingexam.domain.SubjectType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Set;

public record DatingExamSubjectInfoWithSubjectSubmission(
    long id,
    @Schema(implementation = SubjectType.class)
    String type,
    String name,
    boolean isSubmitted,
    List<DatingExamQuestionInfo> questions
) {
    public DatingExamSubjectInfoWithSubjectSubmission(
        DatingExamSubjectInfo subjectInfo,
        Set<DatingExamSubmit> submits
    ) {
        this(
            subjectInfo.id(),
            subjectInfo.type(),
            subjectInfo.name(),
            submits.stream().anyMatch(submit -> submit.getSubjectId().equals(subjectInfo.id())),
            subjectInfo.questions()
        );
    }
}
