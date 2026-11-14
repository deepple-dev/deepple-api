package kr.co.deepple.interview.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InterviewAnswerSaveRequest(
    @NotNull(message = "인터뷰 질문 ID는 필수입니다.") Long interviewQuestionId,
    @Schema(description = "인터뷰 답변 내용", example = "저의 취미는 독서입니다.")
        @NotBlank(message = "인터뷰 답변 내용은 필수입니다.")
        String answerContent) {}
