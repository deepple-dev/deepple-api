package kr.co.deepple.interview.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.interview.domain.InterviewCategory;

public record InterviewQuestionView(
    Long questionId,
    String questionContent,
    @Schema(implementation = InterviewCategory.class) String category,
    @Schema(description = "질문 설명. 질문 카드와 답변 입력칸 안내에 쓴다. 없으면 null이다") String description,
    boolean isAnswered,
    Long answerId,
    String answerContent) {
  @QueryProjection
  public InterviewQuestionView {}
}
