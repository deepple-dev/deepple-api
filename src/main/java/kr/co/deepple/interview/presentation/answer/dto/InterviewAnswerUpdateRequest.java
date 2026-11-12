package kr.co.deepple.interview.presentation.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record InterviewAnswerUpdateRequest(
    @Schema(description = "인터뷰 답변 내용", example = "저의 취미는 독서입니다.")
        @NotBlank(message = "인터뷰 답변 내용은 필수입니다.")
        String answerContent) {}
