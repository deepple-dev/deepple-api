package kr.co.deepple.interview.query.question.view;

import com.querydsl.core.annotations.QueryProjection;
import kr.co.deepple.interview.command.domain.question.InterviewCategory;
import io.swagger.v3.oas.annotations.media.Schema;

public record InterviewQuestionView(
    Long questionId,
    String questionContent,
    @Schema(implementation = InterviewCategory.class)
    String category,
    boolean isAnswered,
    Long answerId,
    String answerContent
) {
    @QueryProjection
    public InterviewQuestionView {
    }
}
