package kr.co.deepple.mission.adapter.out.persistence;

import java.util.List;
import kr.co.deepple.mission.application.port.out.MissionCommandRepository;
import kr.co.deepple.mission.domain.ActionType;
import kr.co.deepple.mission.domain.Mission;
import kr.co.deepple.mission.domain.TargetGender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MissionCommandRepositoryImpl implements MissionCommandRepository {

  private final MissionJpaRepository missionJpaRepository;

  @Override
  public List<Mission> findByActionTypeAndTargetGender(
      ActionType actionType, TargetGender targetGender) {
    return missionJpaRepository.findByActionTypeAndTargetGender(actionType, targetGender);
  }
}
