package kr.co.deepple.mission.application.port.in;

public interface FindMissionRewardUseCase {
  Integer findNextRewardedHeartAmount(long memberId, String actionType);
}
