package kr.co.deepple.profile.fixture;

import java.time.LocalDateTime;
import java.util.Set;
import kr.co.deepple.member.application.port.in.MemberDetail;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberDetailFixture {

  private long memberId = 1L;
  private String activityStatus = "ACTIVE";
  private String grade = "SILVER";
  private boolean vip;
  private boolean profilePublic;
  private boolean datingExamSubmitted;
  private String primaryContactType = "NONE";
  private String phoneNumber = "01012345678";
  private String kakaoId = "kakaoId";
  private String nickname = "딥플";
  private String gender = "MALE";
  private Integer yearOfBirth = 1997;
  private Integer height = 175;
  private String job = "RESEARCH_AND_ENGINEERING";
  private String highestEducation = "BACHELORS_SEOUL";
  private String city = "SEOUL";
  private String district = "GANGNAM_GU";
  private String mbti = "ENFP";
  private String smokingStatus = "NONE";
  private String drinkingStatus = "SOCIAL";
  private String religion = "BUDDHIST";
  private Set<String> hobbies = Set.of("TRAVEL");
  private LocalDateTime lastAccessedAt;
  private LocalDateTime joinedAt = LocalDateTime.of(2026, 1, 1, 0, 0);
  private LocalDateTime deletedAt;

  public static MemberDetailFixture builder() {
    return new MemberDetailFixture();
  }

  public MemberDetail build() {
    return new MemberDetail(
        memberId,
        activityStatus,
        grade,
        vip,
        profilePublic,
        datingExamSubmitted,
        primaryContactType,
        phoneNumber,
        kakaoId,
        nickname,
        gender,
        yearOfBirth,
        height,
        job,
        highestEducation,
        city,
        district,
        mbti,
        smokingStatus,
        drinkingStatus,
        religion,
        hobbies,
        lastAccessedAt,
        joinedAt,
        deletedAt);
  }
}
