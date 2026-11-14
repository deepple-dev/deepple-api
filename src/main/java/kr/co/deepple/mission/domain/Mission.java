package kr.co.deepple.mission.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "missions")
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
    if (requiredAttempt <= 0) {
      throw new IllegalArgumentException(
          "requiredAttempt는 0보다 커야 합니다. requiredAttempt=" + requiredAttempt);
    }
    if (repeatableCount <= 0) {
      throw new IllegalArgumentException(
          "repeatableCount는 0보다 커야 합니다. repeatableCount=" + repeatableCount);
    }
    if (rewardedHeart <= 0) {
      throw new IllegalArgumentException(
          "rewardedHeart는 0보다 커야 합니다. rewardedHeart=" + rewardedHeart);
    }
    this.actionType = actionType;
    this.frequencyType = frequencyType;
    this.targetGender = targetGender;
    this.requiredAttempt = requiredAttempt;
    this.repeatableCount = repeatableCount;
    this.rewardedHeart = rewardedHeart;
    this.isPublic = isPublic;
  }

  public static Mission of(
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

  public String periodOf(LocalDate date) {
    return frequencyType.periodOf(date);
  }
}
