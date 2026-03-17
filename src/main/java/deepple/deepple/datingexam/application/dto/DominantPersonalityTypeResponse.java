package deepple.deepple.datingexam.application.dto;

import deepple.deepple.datingexam.domain.AnswerPersonalityType;
import deepple.deepple.datingexam.domain.DatingExamSubmitResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record DominantPersonalityTypeResponse(
    @Schema(implementation = AnswerPersonalityType.class)
    String personalityType,
    int decisiveIndependentCount,
    int growingRunningMateCount,
    int devotedRomanticCount,
    int realisticShelterCount
) {
    public static DominantPersonalityTypeResponse from(DatingExamSubmitResult result) {
        return new DominantPersonalityTypeResponse(
            result.getDominantPersonalityType().name(),
            result.getDecisiveIndependentCount(),
            result.getGrowingRunningMateCount(),
            result.getDevotedRomanticCount(),
            result.getRealisticShelterCount()
        );
    }
}
