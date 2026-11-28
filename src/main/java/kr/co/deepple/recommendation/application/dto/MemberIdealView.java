package kr.co.deepple.recommendation.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

public record MemberIdealView(
    @Schema(description = "최소 나이. 연 나이(올해 − 태어난 해)", example = "25") Integer minAge,
    @Schema(description = "최대 나이. 연 나이(올해 − 태어난 해)", example = "35") Integer maxAge,
    Set<String> hobbies,
    Set<String> cities,
    String religion,
    String smokingStatus,
    String drinkingStatus) {
  @QueryProjection
  public MemberIdealView {}
}
