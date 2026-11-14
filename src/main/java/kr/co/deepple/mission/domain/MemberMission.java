package kr.co.deepple.mission.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "member_mission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberMission extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long memberId;

  private long missionId;

  private String period;

  private int attemptCount;

  private int successCount;

  private boolean isCompleted;

  private MemberMission(
      long memberId,
      long missionId,
      @NonNull String period,
      int attemptCount,
      int successCount,
      boolean isCompleted) {
    if (attemptCount < 0) {
      throw new IllegalArgumentException("attemptCount는 음수일 수 없습니다. attemptCount=" + attemptCount);
    }
    if (successCount < 0) {
      throw new IllegalArgumentException("successCount는 음수일 수 없습니다. successCount=" + successCount);
    }
    this.memberId = memberId;
    this.missionId = missionId;
    this.period = period;
    this.attemptCount = attemptCount;
    this.successCount = successCount;
    this.isCompleted = isCompleted;
  }

  public static MemberMission of(long memberId, long missionId, String period) {
    return new MemberMission(memberId, missionId, period, 0, 0, false);
  }

  public boolean countPlus(int requiredAttempt, int repeatableCount) {
    attemptCount++;
    if (attemptCount < requiredAttempt) {
      return false;
    }
    successCount++;
    attemptCount = 0;
    if (successCount >= repeatableCount) {
      isCompleted = true;
    }
    return true;
  }
}
