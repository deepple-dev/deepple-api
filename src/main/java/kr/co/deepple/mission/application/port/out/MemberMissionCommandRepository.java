package kr.co.deepple.mission.application.port.out;

import kr.co.deepple.mission.domain.MemberMission;

public interface MemberMissionCommandRepository {
  MemberMission findOrCreateForUpdate(long memberId, long missionId, String period);
}
