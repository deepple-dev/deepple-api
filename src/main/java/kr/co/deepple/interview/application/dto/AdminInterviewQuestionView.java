package kr.co.deepple.interview.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.co.deepple.interview.domain.InterviewCategory;

public record AdminInterviewQuestionView(
    Long id,
    String content,
    @Schema(implementation = InterviewCategory.class) String category,
    Boolean isPublic,
    LocalDateTime createdAt) {
  @QueryProjection
  public AdminInterviewQuestionView {}
}
