package kr.co.deepple.member.application.port.in;

import java.time.LocalDateTime;

public record MemberIdSearchCondition(
    String nickname, String phoneNumber, LocalDateTime joinedFrom, LocalDateTime joinedTo) {

  public boolean isEmpty() {
    return nickname == null && phoneNumber == null && joinedFrom == null && joinedTo == null;
  }
}
