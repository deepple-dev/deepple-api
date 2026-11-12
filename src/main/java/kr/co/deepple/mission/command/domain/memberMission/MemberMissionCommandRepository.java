package kr.co.deepple.mission.command.domain.memberMission;

public interface MemberMissionCommandRepository {
  MemberMission findOrCreateForUpdate(long memberId, long missionId, String period);
}
