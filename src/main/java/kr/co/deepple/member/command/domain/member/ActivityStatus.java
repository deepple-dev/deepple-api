package kr.co.deepple.member.command.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityStatus {
  INITIAL("가입완료"),
  ACTIVE("활동중"),
  SUSPENDED_TEMPORARILY("일시 정지"),
  SUSPENDED_PERMANENTLY("영구 정지"),
  WAITING_SCREENING("심사 대기중"),
  REJECTED_SCREENING("심사 거절"),
  DORMANT("휴면"),
  DELETED("탈퇴");

  private final String description;
}
