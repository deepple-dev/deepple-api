package kr.co.deepple.mission.command.domain.memberMission;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberMission extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long memberId;

  private long missionId;

  // 미션을 성공하기 위한 시도 횟수. 해당 미션의 설정 값을 넘으면, successCount 증가.
  private int attemptCount;

  // 반복 미션의 경우, 해당 미션을 얼마나 수행했는지에 대한 값.
  private int successCount;

  // 해당 미션을 완전히 수행하였는지에 대한 여부.
  private boolean isCompleted;

  private MemberMission(
      long memberId, long missionId, int attemptCount, int successCount, boolean isCompleted) {
    setAttemptCount(attemptCount);
    setSuccessCount(successCount);
    this.memberId = memberId;
    this.missionId = missionId;
    this.attemptCount = attemptCount;
    this.successCount = successCount;
    this.isCompleted = isCompleted;
  }

  /** 초기 미션 생성 시, 시도횟수, 성공횟수를 0, 성공 여부를 false 로 초기화합니다. 추후, 내부 메서드를 통해 해당 값 변경. */
  public static MemberMission create(long memberId, long missionId) {
    return new MemberMission(memberId, missionId, 0, 0, false);
  }

  private void setAttemptCount(int attemptCount) {
    validateAttemptCount(attemptCount);
    this.attemptCount = attemptCount;
  }

  private void setSuccessCount(int successCount) {
    validateSuccessCount(successCount);
    this.successCount = successCount;
  }

  private void validateAttemptCount(int attemptCount) {
    if (attemptCount < 0) {
      throw new IllegalArgumentException("attemptCount는 음수일 수 없습니다. attemptCount=" + attemptCount);
    }
  }

  private void validateSuccessCount(int successCount) {
    if (successCount < 0) {
      throw new IllegalArgumentException("successCount는 음수일 수 없습니다. successCount=" + successCount);
    }
  }

  public void countPlus(int requiredAttempt, int repeatableCount) {
    attemptCount++;
    if (attemptCount == requiredAttempt) {
      successCount++;
      attemptCount = 0;
      if (successCount == repeatableCount) {
        isCompleted = true;
      }
    }
  }
}
