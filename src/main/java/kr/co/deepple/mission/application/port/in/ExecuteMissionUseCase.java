package kr.co.deepple.mission.application.port.in;

public interface ExecuteMissionUseCase {
  MissionExecutionResult executeMissionsByAction(long memberId, String actionType);
}
