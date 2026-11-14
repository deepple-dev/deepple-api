package kr.co.deepple.mission.adapter.out.persistence;

import java.time.LocalDateTime;
import kr.co.deepple.mission.application.port.out.MemberMissionCommandRepository;
import kr.co.deepple.mission.domain.MemberMission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberMissionCommandRepositoryImpl implements MemberMissionCommandRepository {

  private final MemberMissionJpaRepository memberMissionJpaRepository;

  @Override
  public MemberMission findOrCreateForUpdate(long memberId, long missionId, String period) {
    memberMissionJpaRepository.insertIfAbsent(memberId, missionId, period, LocalDateTime.now());
    return memberMissionJpaRepository
        .findForUpdate(memberId, missionId, period)
        .orElseThrow(() ->
            new IllegalStateException(
                "회원 미션이 없습니다. memberId="
                    + memberId
                    + ", missionId="
                    + missionId
                    + ", period="
                    + period));
  }
}
