package kr.co.deepple.match.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MatchType {
  MATCH("매칭"),
  SOULMATE("소울메이트");

  private final String description;
}
