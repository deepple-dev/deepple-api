package kr.co.deepple.interview.presentation.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.co.deepple.interview.command.domain.question.InterviewCategory;

public record InterviewQuestionSaveRequest(
    @Schema(description = "인터뷰 질문 내용", example = "내가 생각하는 나의 장점과 단점")
        @NotBlank(message = "인터뷰 질문 내용은 필수입니다.")
        String questionContent,
    @Schema(implementation = InterviewCategory.class) @NotBlank(message = "인터뷰 질문 카테고리는 필수입니다.")
        String category,
    @Schema(description = "공개 여부", example = "true") @NotNull(message = "공개 여부는 필수입니다.")
        Boolean isPublic) {}
