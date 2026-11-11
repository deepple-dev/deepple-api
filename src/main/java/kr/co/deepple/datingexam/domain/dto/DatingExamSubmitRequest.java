package kr.co.deepple.datingexam.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DatingExamSubmitRequest(
    @NotNull
    Long subjectId,
    @NotNull
    @NotEmpty
    @Valid
    List<AnswerSubmitRequest> answers
) {
}
