package kr.co.deepple.common.enums;

import lombok.Getter;

@Getter
public enum Role {
  MEMBER("회원"),
  ADMIN("일반 관리자"),
  SUPER_ADMIN("슈퍼 관리자");

  private final String description;

  Role(String description) {
    this.description = description;
  }
}
