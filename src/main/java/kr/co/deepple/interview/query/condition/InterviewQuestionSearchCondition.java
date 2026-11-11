package kr.co.deepple.interview.query.condition;

import kr.co.deepple.interview.command.domain.question.InterviewCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record InterviewQuestionSearchCondition(
    @Schema(implementation = InterviewCategory.class)
    @NotBlank(message = "인터뷰 카테고리를 입력해주세요.")
    String category
) {
}
