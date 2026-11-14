package kr.co.deepple.mission.application.port.in;

public interface ExecuteMissionUseCase {
  boolean executeMissionsByAction(long memberId, String actionType);
}
