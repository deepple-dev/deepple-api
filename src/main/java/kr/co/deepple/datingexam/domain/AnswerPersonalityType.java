package kr.co.deepple.datingexam.domain;

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
}
