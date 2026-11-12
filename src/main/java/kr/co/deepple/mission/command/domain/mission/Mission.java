package kr.co.deepple.mission.command.domain.mission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Table(name = "missions")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mission extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private ActionType actionType;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private FrequencyType frequencyType;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private TargetGender targetGender;

  private int requiredAttempt;

  private int repeatableCount;

  private int rewardedHeart;

  private boolean isPublic;

  private Mission(
      @NonNull ActionType actionType,
      @NonNull FrequencyType frequencyType,
      @NonNull TargetGender targetGender,
      int requiredAttempt,
      int repeatableCount,
      int rewardedHeart,
      boolean isPublic) {
    setRequiredAttempt(requiredAttempt);
    setRepeatableCount(repeatableCount);
    setRewardedHeart(rewardedHeart);
    this.actionType = actionType;
    this.frequencyType = frequencyType;
    this.targetGender = targetGender;
    this.isPublic = isPublic;
  }

  public static Mission create(
      ActionType actionType,
      FrequencyType frequencyType,
      TargetGender targetGender,
      int requiredAttempt,
      int repeatableCount,
      int rewardedHeart,
      boolean isPublic) {
    return new Mission(
        actionType,
        frequencyType,
        targetGender,
        requiredAttempt,
        repeatableCount,
        rewardedHeart,
        isPublic);
  }

  private void setRequiredAttempt(int requiredAttempt) {
    validateRequiredAttempt(requiredAttempt);
    this.requiredAttempt = requiredAttempt;
  }

  private void setRepeatableCount(int repeatableCount) {
    validateRepeatableCount(repeatableCount);
    this.repeatableCount = repeatableCount;
  }

  private void setRewardedHeart(int rewardedHeart) {
    validateRewardedHearts(rewardedHeart);
    this.rewardedHeart = rewardedHeart;
  }

  private void validateRequiredAttempt(int requiredAttempt) {
    if (requiredAttempt <= 0) {
      throw new IllegalArgumentException(
          "requiredAttempt는 0보다 커야 합니다. requiredAttempt=" + requiredAttempt);
    }
  }

  private void validateRepeatableCount(int repeatableCount) {
    if (repeatableCount <= 0) {
      throw new IllegalArgumentException(
          "repeatableCount는 0보다 커야 합니다. repeatableCount=" + repeatableCount);
    }
  }

  private void validateRewardedHearts(int rewardedHeart) {
    if (rewardedHeart <= 0) {
      throw new IllegalArgumentException(
          "rewardedHeart는 0보다 커야 합니다. rewardedHeart=" + rewardedHeart);
    }
  }
}
