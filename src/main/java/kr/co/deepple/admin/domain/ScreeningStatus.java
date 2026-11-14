package kr.co.deepple.admin.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScreeningStatus {
  PENDING("대기"),
  APPROVED("승인"),
  REJECTED("반려");

  private final String description;
}
