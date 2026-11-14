package kr.co.deepple.member.application.port.in;

import java.time.LocalDateTime;
import java.util.Set;
import kr.co.deepple.member.domain.AgeConverter;

public record MemberDetail(
    long memberId,
    String activityStatus,
    String grade,
    boolean vip,
    boolean profilePublic,
    boolean datingExamSubmitted,
    String primaryContactType,
    String phoneNumber,
    String kakaoId,
    String nickname,
    String gender,
    Integer yearOfBirth,
    Integer height,
    String job,
    String highestEducation,
    String city,
    String district,
    String mbti,
    String smokingStatus,
    String drinkingStatus,
    String religion,
    Set<String> hobbies,
    LocalDateTime lastAccessedAt,
    LocalDateTime joinedAt,
    LocalDateTime deletedAt) {

  public Integer age() {
    return AgeConverter.toAge(yearOfBirth);
  }
}
