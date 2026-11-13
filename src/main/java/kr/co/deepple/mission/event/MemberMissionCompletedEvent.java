package kr.co.deepple.mission.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MemberMissionCompletedEvent(
    long memberId,
    long memberMissionId,
    int successCount,
    long rewardHeartAmount,
    String memberNickname,
    String actionType,
    @NonNull EventMetadata metadata)
    implements Event {

  public static MemberMissionCompletedEvent of(
      long memberId,
      long memberMissionId,
      int successCount,
      String memberNickname,
      long rewardedHeart,
      String actionType) {
    return new MemberMissionCompletedEvent(
        memberId,
        memberMissionId,
        successCount,
        rewardedHeart,
        memberNickname,
        actionType,
        EventMetadata.create());
  }
}
