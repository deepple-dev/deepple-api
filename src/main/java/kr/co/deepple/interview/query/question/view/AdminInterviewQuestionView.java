package kr.co.deepple.interview.query.question.view;

import com.querydsl.core.annotations.QueryProjection;
import kr.co.deepple.interview.command.domain.question.InterviewCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminInterviewQuestionView(
    Long id,
    String content,
    @Schema(implementation = InterviewCategory.class)
    String category,
    Boolean isPublic,
    LocalDateTime createdAt
) {
    @QueryProjection
    public AdminInterviewQuestionView {
    }
}
