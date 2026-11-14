package kr.co.deepple.interview.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.deepple.interview.domain.InterviewCategory;

public record InterviewQuestionSearchCondition(
    @Schema(implementation = InterviewCategory.class) @NotBlank(message = "인터뷰 카테고리를 입력해주세요.")
        String category) {}
