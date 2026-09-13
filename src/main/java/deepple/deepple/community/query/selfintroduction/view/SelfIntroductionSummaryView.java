package deepple.deepple.community.query.selfintroduction.view;

import com.querydsl.core.annotations.QueryProjection;
import deepple.deepple.datingexam.domain.AnswerPersonalityType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record SelfIntroductionSummaryView(
    Long id,
    String nickname,
    Integer yearOfBirth,
    String title,
    String content,
    String imageUrl,
    LocalDateTime createdAt,
    @Schema(implementation = AnswerPersonalityType.class, description = AnswerPersonalityType.SCHEMA_DESCRIPTION)
    String personalityType
) {
    @QueryProjection
    public SelfIntroductionSummaryView {
    }
}
