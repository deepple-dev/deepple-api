package kr.co.deepple.like.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.mission.application.port.in.MissionExecutionResult;

public record LikeSendResponse(
    boolean hasProcessedMission,
    @Schema(description = "이번에 받은 하트 개수. 받지 않았으면 null", nullable = true)
        Integer rewardedHeartAmount) {

  public static LikeSendResponse from(MissionExecutionResult result) {
    return new LikeSendResponse(result.hasProcessedMission(), result.rewardedHeartAmount());
  }
}
