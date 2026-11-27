package kr.co.deepple.datingexam.domain;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnswerPersonalityType {
  DECISIVE_INDEPENDENT("자기주도형"),
  GROWING_RUNNING_MATE("동반성장형"),
  DEVOTED_ROMANTIC("정서교감형"),
  REALISTIC_SHELTER("현실안정형"),
  STIMULATING_ADVENTURER("자극모험형"),
  RATIONAL_CENTRIST("이성중심형");

  public static final String SCHEMA_DESCRIPTION =
      "연애가치관 유형 - DECISIVE_INDEPENDENT: 자기주도형, GROWING_RUNNING_MATE: 동반성장형, "
          + "DEVOTED_ROMANTIC: 정서교감형, REALISTIC_SHELTER: 현실안정형, "
          + "STIMULATING_ADVENTURER: 자극모험형, RATIONAL_CENTRIST: 이성중심형";

  private final String description;

  public List<AnswerPersonalityType> compatibleTypes() {
    return switch (this) {
      case DECISIVE_INDEPENDENT -> List.of(GROWING_RUNNING_MATE, DEVOTED_ROMANTIC);
      case GROWING_RUNNING_MATE -> List.of(DEVOTED_ROMANTIC, REALISTIC_SHELTER);
      case DEVOTED_ROMANTIC -> List.of(REALISTIC_SHELTER, STIMULATING_ADVENTURER);
      case REALISTIC_SHELTER -> List.of(STIMULATING_ADVENTURER, RATIONAL_CENTRIST);
      case STIMULATING_ADVENTURER -> List.of(RATIONAL_CENTRIST, DECISIVE_INDEPENDENT);
      case RATIONAL_CENTRIST -> List.of(DECISIVE_INDEPENDENT, GROWING_RUNNING_MATE);
    };
  }
}
