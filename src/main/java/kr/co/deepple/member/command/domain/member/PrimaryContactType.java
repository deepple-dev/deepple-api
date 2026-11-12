package kr.co.deepple.member.command.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PrimaryContactType {
  KAKAO("카카오ID"),
  PHONE_NUMBER("전화번호"),
  NONE("설정되지 않음");

  private final String description;
}
