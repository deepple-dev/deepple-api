package kr.co.deepple.mission.application.port.in;

public record MissionExecutionResult(boolean hasProcessedMission, Integer rewardedHeartAmount) {

  public static MissionExecutionResult of(boolean hasProcessedMission, int rewardedHeartAmount) {
    return new MissionExecutionResult(
        hasProcessedMission, rewardedHeartAmount > 0 ? rewardedHeartAmount : null);
  }
}
