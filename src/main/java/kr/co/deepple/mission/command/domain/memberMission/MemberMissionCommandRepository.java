package kr.co.deepple.mission.command.domain.memberMission;

import java.util.Optional;

public interface MemberMissionCommandRepository {
  Optional<MemberMission> findByMemberIdAndMissionId(long memberId, long missionId);

  MemberMission save(MemberMission memberMission);

  Optional<MemberMission> findByMemberIdAndMissionIdOnToday(long memberId, long missionId);
}
