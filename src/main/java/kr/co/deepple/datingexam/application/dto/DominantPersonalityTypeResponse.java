package kr.co.deepple.datingexam.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import kr.co.deepple.datingexam.domain.DatingExamSubmitResult;

public record DominantPersonalityTypeResponse(
    @Schema(
            implementation = AnswerPersonalityType.class,
            description = AnswerPersonalityType.SCHEMA_DESCRIPTION)
        String personalityType,
    int decisiveIndependentCount,
    int growingRunningMateCount,
    int devotedRomanticCount,
    int realisticShelterCount,
    int stimulatingAdventurerCount,
    int rationalRealistCount) {
  public static DominantPersonalityTypeResponse from(DatingExamSubmitResult result) {
    return new DominantPersonalityTypeResponse(
        result.getDominantPersonalityType().name(),
        result.getDecisiveIndependentCount(),
        result.getGrowingRunningMateCount(),
        result.getDevotedRomanticCount(),
        result.getRealisticShelterCount(),
        result.getStimulatingAdventurerCount(),
        result.getRationalRealistCount());
  }
}
