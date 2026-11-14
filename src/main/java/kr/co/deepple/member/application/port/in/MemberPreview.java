package kr.co.deepple.member.application.port.in;

import kr.co.deepple.member.domain.AgeConverter;

public record MemberPreview(
    long memberId,
    String nickname,
    String profileImageUrl,
    String city,
    Integer yearOfBirth,
    String gender) {

  public Integer age() {
    return AgeConverter.toAge(yearOfBirth);
  }
}
