package kr.co.deepple.interview.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InterviewAnswerUpdateRequest(
    @Schema(description = "인터뷰 답변 내용. 1000자 이하", example = "저의 취미는 독서입니다.")
        @NotBlank(message = "인터뷰 답변 내용은 필수입니다.")
        @Size(max = 1000, message = "인터뷰 답변 내용은 1000자 이하여야 합니다.")
        String answerContent) {}
