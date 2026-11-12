package kr.co.deepple.mission.command.infra.mission;

import java.util.List;
import kr.co.deepple.mission.command.domain.mission.ActionType;
import kr.co.deepple.mission.command.domain.mission.Mission;
import kr.co.deepple.mission.command.domain.mission.MissionCommandRepository;
import kr.co.deepple.mission.command.domain.mission.TargetGender;
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
