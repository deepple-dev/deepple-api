package kr.co.deepple.datingexam.application.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
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
    int rationalCentristCount,
    @ArraySchema(
            arraySchema =
                @Schema(description = "나와 궁합이 좋은 유형 2개(결과 화면 순서). 값은 personalityType과 같다"),
            schema = @Schema(implementation = AnswerPersonalityType.class))
        List<String> compatibleTypes) {
  public static DominantPersonalityTypeResponse from(DatingExamSubmitResult result) {
    return new DominantPersonalityTypeResponse(
        result.getDominantPersonalityType().name(),
        result.getDecisiveIndependentCount(),
        result.getGrowingRunningMateCount(),
        result.getDevotedRomanticCount(),
        result.getRealisticShelterCount(),
        result.getStimulatingAdventurerCount(),
        result.getRationalCentristCount(),
        result.getDominantPersonalityType().compatibleTypes().stream()
            .map(AnswerPersonalityType::name)
            .toList());
  }
}
