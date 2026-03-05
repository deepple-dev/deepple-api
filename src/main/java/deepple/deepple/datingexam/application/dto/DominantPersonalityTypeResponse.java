package deepple.deepple.datingexam.application.dto;

import deepple.deepple.datingexam.domain.AnswerPersonalityType;
import io.swagger.v3.oas.annotations.media.Schema;

public record DominantPersonalityTypeResponse(
    @Schema(implementation = AnswerPersonalityType.class)
    String personalityType
) {
}
