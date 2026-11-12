package kr.co.deepple.datingexam.domain.dto;

import jakarta.validation.constraints.NotNull;

public record AnswerSubmitRequest(
    @NotNull
    Long questionId,
    @NotNull
    Long answerId
) {
}
