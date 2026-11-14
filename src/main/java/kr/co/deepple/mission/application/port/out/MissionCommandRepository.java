package kr.co.deepple.mission.application.port.out;

import java.util.List;
import kr.co.deepple.mission.domain.ActionType;
import kr.co.deepple.mission.domain.Mission;
import kr.co.deepple.mission.domain.TargetGender;

public interface MissionCommandRepository {
  List<Mission> findByActionTypeAndTargetGender(ActionType actionType, TargetGender targetGender);
}
