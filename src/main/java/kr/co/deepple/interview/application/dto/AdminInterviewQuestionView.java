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
    @Schema(description = "질문 설명. 없으면 null이다") String description,
    LocalDateTime createdAt) {
  @QueryProjection
  public AdminInterviewQuestionView {}
}
