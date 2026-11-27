package kr.co.deepple.member.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import java.util.HashSet;
import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProfile {

  private static final int MIN_HEIGHT = 130;
  private static final int MAX_HEIGHT = 220;
  private static final int MAX_HOBBY_COUNT = 3;

  @Embedded private Nickname nickname;

  @Embedded private YearOfBirth yearOfBirth;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private Gender gender;

  private Integer height;

  @ElementCollection
  @CollectionTable(name = "member_hobbies", joinColumns = @JoinColumn(name = "member_id"))
  @Column(name = "name", columnDefinition = "varchar(50)")
  @Enumerated(EnumType.STRING)
  private Set<Hobby> hobbies = new HashSet<>();

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private Job job;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private Mbti mbti;

  @Embedded private Region region;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private Religion religion;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private SmokingStatus smokingStatus;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private DrinkingStatus drinkingStatus;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private HighestEducation highestEducation;

  @Builder
  private MemberProfile(
      Nickname nickname,
      Integer yearOfBirth,
      Gender gender,
      Integer height,
      Mbti mbti,
      Region region,
      Religion religion,
      SmokingStatus smokingStatus,
      DrinkingStatus drinkingStatus,
      HighestEducation highestEducation,
      Job job,
      Set<Hobby> hobbies) {
    validateHeight(height);
    validateHobbies(hobbies);
    this.nickname = nickname;
    this.yearOfBirth = YearOfBirth.from(yearOfBirth);
    this.gender = gender;
    this.height = height;
    this.mbti = mbti;
    this.region = region;
    this.religion = religion;
    this.smokingStatus = smokingStatus;
    this.drinkingStatus = drinkingStatus;
    this.highestEducation = highestEducation;
    this.job = job;
    this.hobbies = hobbies;
  }

  private static void validateHeight(Integer height) {
    if (height != null && (height < MIN_HEIGHT || height > MAX_HEIGHT)) {
      throw new DeeppleException(MemberErrorCode.INVALID_HEIGHT, "height=" + height);
    }
  }

  private static void validateHobbies(Set<Hobby> hobbies) {
    if (hobbies != null && hobbies.size() > MAX_HOBBY_COUNT) {
      throw new DeeppleException(MemberErrorCode.HOBBY_COUNT_EXCEEDED, "count=" + hobbies.size());
    }
  }

  public boolean isProfileSettingNeeded() {
    return nickname == null
        || yearOfBirth == null
        || yearOfBirth.getValue() == null
        || gender == null
        || height == null
        || job == null
        || hobbies == null
        || hobbies.isEmpty()
        || mbti == null
        || region == null
        || religion == null
        || smokingStatus == null
        || drinkingStatus == null
        || highestEducation == null;
  }
}
