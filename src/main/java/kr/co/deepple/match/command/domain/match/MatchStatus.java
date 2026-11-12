package kr.co.deepple.match.command.domain.match;

import lombok.Getter;

@Getter
public enum MatchStatus {
  WAITING("응답 대기중"),
  MATCHED("매칭 완료"),
  EXPIRED("만료"),
  REJECT_CHECKED("거절 확인"),
  REJECTED("거절");

  private final String description;

  MatchStatus(String description) {
    this.description = description;
  }
}
