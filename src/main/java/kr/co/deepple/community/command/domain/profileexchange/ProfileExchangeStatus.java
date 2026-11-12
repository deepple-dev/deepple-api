package kr.co.deepple.community.command.domain.profileexchange;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProfileExchangeStatus {
  WAITING("응답 대기중"),
  APPROVE("교환 수락"),
  REJECTED("교환 거절");

  private final String description;
}
