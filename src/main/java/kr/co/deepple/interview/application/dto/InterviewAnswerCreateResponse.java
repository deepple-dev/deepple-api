package kr.co.deepple.interview.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.mission.application.port.in.MissionExecutionResult;

public record InterviewAnswerCreateResponse(
    boolean hasProcessedMission,
    @Schema(description = "이번에 받은 하트 개수. 받지 않았으면 null", nullable = true)
        Integer rewardedHeartAmount) {

  public static InterviewAnswerCreateResponse from(MissionExecutionResult result) {
    return new InterviewAnswerCreateResponse(
        result.hasProcessedMission(), result.rewardedHeartAmount());
  }
}
