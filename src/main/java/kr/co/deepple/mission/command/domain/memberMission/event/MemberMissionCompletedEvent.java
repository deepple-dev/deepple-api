package kr.co.deepple.mission.command.domain.memberMission.event;

import java.util.Map;
import kr.co.deepple.common.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MemberMissionCompletedEvent extends Event {

  private final long memberId;
  private final long memberMissionId;
  private final int successCount;
  private final long rewardHeartAmount;
  private final String memberNickname;
  private final String actionType;

  public static MemberMissionCompletedEvent of(
      long memberId,
      long memberMissionId,
      int successCount,
      String memberNickname,
      long rewardedHeart,
      String actionType) {
    return new MemberMissionCompletedEvent(
        memberId, memberMissionId, successCount, rewardedHeart, memberNickname, actionType);
  }

  @Override
  public Map<String, Object> logAttributes() {
    return Map.of(
        "memberId", memberId, "memberMissionId", memberMissionId, "successCount", successCount);
  }
}
