package kr.co.deepple.feed.domain;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedPersonalityType {
  DECISIVE_INDEPENDENT("자기주도형"),
  GROWING_RUNNING_MATE("동반성장형"),
  DEVOTED_ROMANTIC("정서교감형"),
  REALISTIC_SHELTER("현실안정형"),
  STIMULATING_ADVENTURER("자극모험형"),
  RATIONAL_CENTRIST("이성중심형");

  private final String displayName;

  public static String displayNameOf(String code) {
    return Arrays.stream(values())
        .filter(type -> type.name().equals(code))
        .findFirst()
        .map(FeedPersonalityType::getDisplayName)
        .orElse(null);
  }
}
