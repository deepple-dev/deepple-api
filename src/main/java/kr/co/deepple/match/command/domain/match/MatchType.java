package kr.co.deepple.match.command.domain.match;

import lombok.Getter;

@Getter
public enum MatchType {
  MATCH("매칭"),
  SOULMATE("소울메이트");

  private final String description;

  MatchType(String description) {
    this.description = description;
  }
}
