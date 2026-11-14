package kr.co.deepple.like.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LikeLevel {
  INTERESTED("관심있어요"),
  HIGHLY_INTERESTED("매우 관심있어요");

  private final String description;
}
