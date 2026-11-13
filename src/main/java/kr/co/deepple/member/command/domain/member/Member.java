package kr.co.deepple.member.command.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.co.deepple.common.entity.SoftDeleteBaseEntity;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.member.vo.KakaoId;
import kr.co.deepple.member.command.domain.member.vo.MemberProfile;
import kr.co.deepple.member.command.domain.member.vo.PhoneNumber;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "members")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member extends SoftDeleteBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded private PhoneNumber phoneNumber;

  @Embedded private KakaoId kakaoId;

  @Embedded private MemberProfile profile;

  private boolean isVip;

  @Builder.Default private boolean isProfilePublic = false;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private Grade grade;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private ActivityStatus activityStatus;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private PrimaryContactType primaryContactType;

  @Builder.Default private boolean isDatingExamSubmitted = false;

  private LocalDateTime lastAccessedAt;

  public static Member fromPhoneNumber(@NonNull String phoneNumber) {
    return Member.builder()
        .phoneNumber(PhoneNumber.from(phoneNumber))
        .activityStatus(ActivityStatus.INITIAL)
        .isVip(false)
        .primaryContactType(PrimaryContactType.NONE)
        .grade(Grade.SILVER)
        .build();
  }

  public void updateProfile(@NonNull MemberProfile profile) {
    this.profile = profile;
  }

  public boolean isProfileInitialized() {
    return !isProfileSettingNeeded() && activityStatus == ActivityStatus.INITIAL;
  }

  public boolean isProfileSettingNeeded() {
    if (profile == null) {
      return true;
    }
    return profile.isProfileSettingNeeded();
  }

  public String getKakaoId() {
    if (kakaoId == null) {
      return null;
    }
    return kakaoId.getValue();
  }

  public String getPhoneNumber() {
    return phoneNumber.getValue();
  }

  private void changeToDormant() {
    activityStatus = ActivityStatus.DORMANT;
  }

  private void changeToActive() {
    activityStatus = ActivityStatus.ACTIVE;
  }

  public void deactivate() {
    if (!isActive()) {
      throw new DeeppleException(
          MemberErrorCode.INVALID_ACTIVITY_STATUS_CHANGE,
          "활동중인 회원만 휴면으로 전환할 수 있습니다." + " memberId=" + id + ", activityStatus=" + activityStatus);
    }
    changeToDormant();
  }

  public void reactivate() {
    if (!isDormant()) {
      throw new DeeppleException(
          MemberErrorCode.INVALID_ACTIVITY_STATUS_CHANGE,
          "휴면 회원만 휴면을 해제할 수 있습니다." + " memberId=" + id + ", activityStatus=" + activityStatus);
    }
    changeToActive();
  }

  public void approveScreening() {
    changeToActive();
  }

  public void changeToRejected() {
    activityStatus = ActivityStatus.REJECTED_SCREENING;
  }

  public void changeToWaiting() {
    activityStatus = ActivityStatus.WAITING_SCREENING;
  }

  public void changePrimaryContactTypeToKakao(KakaoId kakaoId) {
    this.kakaoId = kakaoId;
    this.primaryContactType = PrimaryContactType.KAKAO;
  }

  public void changePrimaryContactTypeToPhoneNumber(@NonNull String phoneNumber) {
    this.phoneNumber = PhoneNumber.from(phoneNumber);
    this.primaryContactType = PrimaryContactType.PHONE_NUMBER;
  }

  public boolean isActive() {
    return activityStatus == ActivityStatus.ACTIVE;
  }

  public boolean isDormant() {
    return activityStatus == ActivityStatus.DORMANT;
  }

  public Gender getGender() {
    return profile.getGender();
  }

  public void updateGrade(@NonNull Grade grade) {
    this.grade = grade;
  }

  public void updateSetting(@NonNull Grade grade, boolean isVip) {
    this.grade = grade;
    this.isVip = isVip;
  }

  public void publishProfile() {
    isProfilePublic = true;
  }

  public void nonPublishProfile() {
    isProfilePublic = false;
  }

  public void changeActivityStatus(@NonNull ActivityStatus activityStatus) {
    switch (activityStatus) {
      case ActivityStatus.DORMANT -> changeToDormant();
      case ActivityStatus.ACTIVE -> changeToActive();
      default -> this.activityStatus = activityStatus;
    }
  }

  public void markDatingExamSubmitted() {
    if (isDatingExamSubmitted) {
      throw new IllegalStateException("이미 연애 모의고사를 제출한 멤버입니다. member id: " + id);
    }
    isDatingExamSubmitted = true;
  }

  public boolean hasSubmittedDatingExam() {
    return isDatingExamSubmitted;
  }

  @Override
  public void delete() {
    this.activityStatus = ActivityStatus.DELETED;
    super.delete();
  }

  public boolean hasSameGender(Member other) {
    return this.getGender() == other.getGender();
  }
}
