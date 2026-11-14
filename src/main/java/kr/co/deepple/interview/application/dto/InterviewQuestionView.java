package kr.co.deepple.interview.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.interview.domain.InterviewCategory;

public record InterviewQuestionView(
    Long questionId,
    String questionContent,
    @Schema(implementation = InterviewCategory.class) String category,
    boolean isAnswered,
    Long answerId,
    String answerContent) {
  @QueryProjection
  public InterviewQuestionView {}
}
