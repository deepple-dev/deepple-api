package kr.co.deepple.match.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MatchStatus {
  WAITING("응답 대기중"),
  MATCHED("매칭 완료"),
  EXPIRED("만료"),
  REJECT_CHECKED("거절 확인"),
  REJECTED("거절");

  private final String description;
}
